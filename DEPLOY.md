# CrolClient - Быстрый деплой

## Что сделать:

### 1. Распаковать архив
```bash
unzip CrolClient.zip
cd CrolClient
```

### 2. Все файлы уже правильно настроены для 1.21.4!

✅ Пакеты: `com.crolclient.buyer.*`
✅ Gradle версия: `fabric_version=0.110.0+1.21.4`
✅ Java 21 (Temurin)
✅ Yarn Official Mappings

### 3. Локально собрать (опционально)
```bash
./gradlew build
# Output: build/libs/crolclient-1.0.0.jar
```

### 4. Пушить в свой GitHub репо

#### Вариант А - если репо уже существует:
```bash
git remote set-url origin https://github.com/ctayak47-web/CrolClient.git
git add -A
git commit -m "Fix: Update all packages to com.crolclient for Minecraft 1.21.4"
git push origin main
```

#### Вариант Б - если хочешь новый репо:
```bash
git init
git add -A
git commit -m "Initial commit: CrolClient for Minecraft 1.21.4"
git remote add origin https://github.com/ctayak47-web/YourNewRepo.git
git push -u origin main
```

### 5. Когда пушишь в GitHub

GitHub Actions автоматически скомпилирует мод:
- Посмотри вкладку **Actions** в репо
- Должно быть зеленое галочка ✅

---

## Структура проекта

```
CrolClient/
├── src/main/java/com/crolclient/buyer/
│   ├── DoubleChestBuyerMod.java (главный класс)
│   ├── gui/
│   │   ├── BuyerScreen.java (главный GUI)
│   │   ├── CategoryButton.java
│   │   ├── ItemSlot.java
│   │   └── HotItemSlot.java
│   └── data/
│       ├── BuyerData.java (логика сохранения)
│       └── BuyerItem.java
├── build.gradle (настройки Gradle)
├── gradle.properties (версии)
├── .github/workflows/build.yml (GitHub Actions)
└── README.md
```

## Команда в игре

```
/buyer
```

Откроет GUI двойного сундука с товарами и горячими предложениями.

---

**Все готово! Просто распакуй → пуши в GitHub → готово! 🎉**
