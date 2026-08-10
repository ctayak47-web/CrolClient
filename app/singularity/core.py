from datetime import datetime, timezone
from . import db

# ─── constants ────────────────────────────────────────────────────────────────

BASE_RATE       = 1.0        # cycles per second at power_level 1
RATE_PER_LEVEL  = 0.8        # additional cycles/s per level
MAX_HEAT        = 100.0
HEAT_PER_COLLECT = 12.0      # heat gained on each collect
HEAT_DECAY_RATE  = 2.0       # heat lost per second (passive cooling)
HEAT_THRESHOLD   = 70.0      # above this → efficiency penalty kicks in
COOLDOWN_COST    = 50.0      # cycles spent to emergency cooldown
UPGRADE_BASE     = 200.0     # cost of level 2
UPGRADE_FACTOR   = 2.2       # cost multiplier per level
MAX_IDLE_SECONDS = 86400 * 3 # cap offline gain at 3 days
CLUSTER_BONUS    = 0.15      # +15% per additional cluster member


def _now():
    return datetime.now(timezone.utc)


def _parse_dt(s):
    if s is None:
        return _now()
    try:
        dt = datetime.fromisoformat(str(s))
        if dt.tzinfo is None:
            dt = dt.replace(tzinfo=timezone.utc)
        return dt
    except Exception:
        return _now()


def _elapsed(ts_str):
    return max(0.0, (_now() - _parse_dt(ts_str)).total_seconds())


def _efficiency(heat):
    if heat <= HEAT_THRESHOLD:
        return 1.0
    # linear drop from 1.0 at threshold to 0.3 at max heat
    ratio = (heat - HEAT_THRESHOLD) / (MAX_HEAT - HEAT_THRESHOLD)
    return max(0.3, 1.0 - 0.7 * ratio)


def _cycle_rate(power_level):
    return BASE_RATE + RATE_PER_LEVEL * (power_level - 1)


def _upgrade_cost(current_level):
    return UPGRADE_BASE * (UPGRADE_FACTOR ** (current_level - 1))


def preview(uid):
    """return pending cycles without modifying state."""
    u = db.get_user(uid)
    if not u:
        return 0.0, 0.0

    elapsed = min(_elapsed(u["last_collect"]), MAX_IDLE_SECONDS)

    # heat decays passively
    heat = max(0.0, u["heat_level"] - HEAT_DECAY_RATE * elapsed)

    # use average efficiency over the period
    avg_eff = (_efficiency(u["heat_level"]) + _efficiency(heat)) / 2
    rate = _cycle_rate(u["power_level"])
    pending = rate * avg_eff * elapsed

    return pending, heat


def collect(uid):
    u = db.get_user(uid)
    if not u:
        return 0.0

    elapsed = min(_elapsed(u["last_collect"]), MAX_IDLE_SECONDS)
    heat = max(0.0, u["heat_level"] - HEAT_DECAY_RATE * elapsed)

    avg_eff = (_efficiency(u["heat_level"]) + _efficiency(heat)) / 2
    rate = _cycle_rate(u["power_level"])
    gained = rate * avg_eff * elapsed

    # apply heat from this collection
    heat = min(MAX_HEAT, heat + HEAT_PER_COLLECT)

    new_balance = u["balance"] + gained
    db.update_user(
        uid,
        balance=round(new_balance, 2),
        heat_level=round(heat, 2),
        last_collect=_now().isoformat(),
    )
    return gained


def upgrade(uid):
    u = db.get_user(uid)
    if not u:
        return False, "user not found"

    cost = _upgrade_cost(u["power_level"])
    if u["balance"] < cost:
        return False, f"need {cost:.0f} cycles (have {u['balance']:.0f})"

    db.update_user(
        uid,
        balance=round(u["balance"] - cost, 2),
        power_level=u["power_level"] + 1,
    )
    return True, u["power_level"] + 1


def cooldown(uid):
    u = db.get_user(uid)
    if not u:
        return False, "user not found"
    if u["balance"] < COOLDOWN_COST:
        return False, f"need {COOLDOWN_COST:.0f} cycles"

    db.update_user(
        uid,
        balance=round(u["balance"] - COOLDOWN_COST, 2),
        heat_level=0.0,
    )
    return True, "core cooled"


# ─── cluster mechanics ────────────────────────────────────────────────────────

def cluster_join(uid, chat_id):
    # leave any existing cluster first
    db.cluster_leave(uid)
    db.cluster_join(uid, chat_id)


def cluster_leave(uid):
    db.cluster_leave(uid)


def cluster_collect(uid, chat_id):
    """collect shared bonus for the caller based on cluster multiplier."""
    members = db.cluster_members(chat_id)
    if not members:
        return 0.0

    count = len(members)
    bonus_mult = 1.0 + CLUSTER_BONUS * (count - 1)

    cluster = db.cluster_info(chat_id)
    if not cluster:
        return 0.0

    elapsed = min(_elapsed(cluster["last_collect"]), MAX_IDLE_SECONDS)
    total_power = sum(m["power_level"] for m in members)

    # pool rate * bonus
    pool_rate = _cycle_rate(total_power) * bonus_mult
    pool_gained = pool_rate * elapsed

    # share proportionally by power_level
    u = db.get_user(uid)
    share = u["power_level"] / total_power if total_power else 0
    gained = pool_gained * share

    db.update_user(uid, balance=round(u["balance"] + gained, 2))
    db.update_cluster(chat_id, last_collect=_now().isoformat())
    return gained


# ─── display helpers ─────────────────────────────────────────────────────────

def user_stats(uid):
    u = db.get_user(uid)
    if not u:
        return None
    pending, projected_heat = preview(uid)
    cost = _upgrade_cost(u["power_level"])
    rate = _cycle_rate(u["power_level"])
    eff = _efficiency(u["heat_level"])
    return {
        "power_level":      u["power_level"],
        "balance":          u["balance"],
        "heat":             u["heat_level"],
        "projected_heat":   projected_heat,
        "efficiency":       eff,
        "rate":             rate,
        "effective_rate":   rate * eff,
        "pending":          pending,
        "upgrade_cost":     cost,
        "last_collect":     u["last_collect"],
        "username":         u["username"],
    }


def cluster_stats(uid, chat_id):
    my_cluster = db.user_cluster(uid)
    members = db.cluster_members(chat_id) if chat_id else []
    count = len(members)
    bonus = 1.0 + CLUSTER_BONUS * (count - 1) if count > 1 else 1.0
    return {
        "in_cluster":   my_cluster == chat_id and my_cluster is not None,
        "member_count": count,
        "bonus_mult":   bonus,
        "members":      members,
        "my_cluster":   my_cluster,
    }
