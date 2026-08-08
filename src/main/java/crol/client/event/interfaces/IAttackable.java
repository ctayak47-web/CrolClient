package crol.client.event.interfaces;

import crol.client.event.classes.AttackEvent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface IAttackable {
   void onAttack(AttackEvent var1);
}
