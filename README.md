# metaworld-core
Ядро для мультимиров, один мир - куча игроков!

## Подключение metaworld-core
Для того чтобы использовать metaworld, нам всего лишь
нужно добавить зависимость в BuildScript (build.gradle)

```groovy
repositories {
    mavenCentral()
    maven {
        url 'https://repo.implario.dev/cristalix/'
        credentials {
            username = System.getenv("IMPLARIO_REPO_USER")
            password = System.getenv("IMPLARIO_REPO_PASSWORD")
        }
    }
}

dependencies {
    implementation 'me.func:metaworld-api:live-SNAPSHOT'
}
```

## Использование metaworld-core

Создадим класс, в котором сделаем примитивное использование
`metaworld-core`.

```kotlin
class Test : JavaPlugin() {
    
    // Загрузка карты, на которой будет
    private val map = WorldMeta(MapLoader.load("func", "test"))
    // Центрируем локацию, чтобы она не было кривой
    val spawn: Label = map.getLabel("spawn").apply {
        x += 0.5
        z += 0.5
        yaw = -90f
    }
    
    // Создаём список построек, которые будут уникальны для каждого игрока
    var list = mutableListOf<Building>()

    override fun onEnable() {
        // Используем метод MetaWorld#universe(), чтобы легко обозначить мир и модификаторы
        MetaWorld.universe(
            // Передаём мир, который мы загрузили и vararg MetaSubscriber для регистрации модификаторов чанка
            map.world, *MetaSubscriber()
                // Обрабатываем список, который мы получили из нашей функции #create()
                .buildingLoader { create() }
                // Модифицируем наш чанк, если мы хотим его описать особенно
                .customModifier { chunk ->
                    chunk.modify(
                        // Указываем позицию блока в чанке
                        BlockPosition(chunk.chunk.locX * 16, 0, chunk.chunk.locZ * 16),
                        Block.getById(9).getBlockData()
                    )
                // Завершаем модификацию чанка, с помощью функции MetaSubcriber#build()    
                }.build()
        )
    }

    // Функция создания построек, чтобы у каждого игрока были свои постройки
    fun create(): List<Building> {
        // Проверяем список, если он пуст добавляем постройку в список
        if (list.isEmpty())
            list.add(Building(UUID.fromString("307264a1-2c69-11e8-b5ea-1cb72caa35fd"), "test", "red", map).apply {
                // Метод размещения постройки на указанных координатах (можно перекрасить блоки)
                allocate(Location(map.world, 0.0, 20.0, 0.0))
                // Позволяет при нажатии на постройку, что-то делать с игроком
            }.onClick { player, packetPlayInUseItem -> player.sendMessage("Привет папаша!") }
                    // Если мы хотим что-то делать, когда ломают блок в нашей локации
                .onBreak { player, packetPlayInBlockDig -> (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutBlockChange().apply {
                    a = packetPlayInBlockDig.a
                    block = MetaWorld.storage[player.uniqueId]!!.buildings[0].allocation!!.blocks?.get(a)!!
                }) })
        // После всего этого возвращаем список, который мы только что сами заполнили
        return list
    }
}
```

Если вы хотите углубиться в использование данной api, вы можете
посмотреть исходный код, там есть подробное описание каждой функции и класса.

