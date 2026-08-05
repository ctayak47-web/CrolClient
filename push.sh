#!/bin/bash

# Быстрый пуш в GitHub для CrolClient
# Просто запусти: bash push.sh

echo "🚀 Пушу CrolClient в GitHub..."

git add -A
git commit -m "Fix: Update all packages to com.crolclient for Minecraft 1.21.4 Fabric"
git push origin main

echo "✅ Готово! Код загружен в GitHub"
