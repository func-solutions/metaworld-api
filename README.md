# metaworld-api
Ядро для мультимиров, один мир - куча игроков!

## Подключение metaworld-api
Для того чтобы использовать metaworld, нам всего лишь
нужно добавить зависимость в BuildScript (build.gradle)

```groovy
repositories {
    mavenCentral()
    maven {
        url 'https://repo.c7x.dev/repository/maven-public/'
        credentials {
            username System.getenv("CRI_REPO_LOGIN")
            password System.getenv("CRI_REPO_PASSWORD")
        }
    }
}

dependencies {
    implementation 'me.func:metaworld-api:1.0.7' // сама библиотека
    implementation 'me.func:world-api:1.0.6' // нужная зависимость для мила
}
```

## Использование metaworld-api

Создадим класс, в котором сделаем примитивное использование
`metaworld-core`.

``Давайте постепенно разберём весь код.``

В данной части кода, мы загрузим карту, на которой мы будем использовать наше апи.
Так же, центрируем локацию, чтобы не было криво.

```kotlin
    private val map = MapLoader.load("func", "test")
    val spawn: Label = map.label("spawn", 0.5, 0.0, 0.5)

    var list = mutableListOf<Building>()
```

`Разделим функцию на несколько частей, чтобы показать все очень подробно`

Создадим функцию, которая нам вернёт список разрешённых построек, а так же в наш список добавим эти самые постройки

```kotlin
fun create(): List<Building> {
        if (list.isEmpty())
            list.add(Building(UUID.fromString("307264a1-2c69-11e8-b5ea-1cb72caa35fd"), "test", "red", map).apply {
                allocate(Location(map.world, 0.0, 20.0, 0.0))
```

У класса Building есть методы: #onClick и #onBreak, здесь мы описывам, что будет происходить, 
если мы будем кликать на постройку или ломать её, так же мы собираем весь список вместе

```kotlin
}.onClick { player, packetPlayInUseItem -> player.sendMessage("Привет папаша!") }
                .onBreak { player, packetPlayInBlockDig -> (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutBlockChange().apply {
                    a = packetPlayInBlockDig.a
                    block = MetaWorld.storage[player.uniqueId]!!.buildings[0].allocation!!.blocks?.get(a)!!
                }) })
        return list
```

`Готовый варинат нашей функции выглядит так:`

```kotlin
    fun create(): List<Building> {
        if (list.isEmpty())
            list.add(Building(UUID.fromString("307264a1-2c69-11e8-b5ea-1cb72caa35fd"), "test", "red", map).apply {
                allocate(Location(map.world, 0.0, 20.0, 0.0))
            }.onClick { player, packetPlayInUseItem -> player.sendMessage("Привет папаша!") }
                .onBreak { player, packetPlayInBlockDig -> (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutBlockChange().apply {
                    a = packetPlayInBlockDig.a
                    block = MetaWorld.storage[player.uniqueId]!!.buildings[0].allocation!!.blocks?.get(a)!!
                }) })
        return list
        }
```

`Разберём, инициализацию metaworld-api:`

Через Metaworld#universe, мы показываем, в каком мире это будет работать, какие могут быть
модификаторы чанка и сразу создаем их. Завершаем создание модификаторов методом #build
```kotlin
    override fun onEnable() {
        MetaWorld.universe(
            map.world, *MetaSubscriber()
                .buildingLoader { create() }
                .customModifier { chunk ->
                    chunk.modify(
                        BlockPosition(chunk.chunk.locX * 16, 0, chunk.chunk.locZ * 16),
                        Block.getById(9).getBlockData()
                    )
                }.build()
        )
    }
```

`В итоге у нас должен получится такой класс:`

```kotlin
class Test : JavaPlugin() {
    
    private val map = WorldMeta(MapLoader.load("func", "test"))
    val spawn: Label = map.label("spawn", 0.5, 0.0, 0.5)
    
    var list = mutableListOf<Building>()

    override fun onEnable() {
        MetaWorld.universe(
            map.world, *MetaSubscriber()
                .buildingLoader { create() }
                .customModifier { chunk ->
                    chunk.modify(
                        BlockPosition(chunk.chunk.locX * 16, 0, chunk.chunk.locZ * 16),
                        Block.getById(9).getBlockData()
                    )
                }.build()
        )
    }
    
    fun create(): List<Building> {
        if (list.isEmpty())
            list.add(Building(UUID.fromString("307264a1-2c69-11e8-b5ea-1cb72caa35fd"), "test", "red", map).apply {
                allocate(Location(map.world, 0.0, 20.0, 0.0))
            }.onClick { player, packetPlayInUseItem -> player.sendMessage("Привет папаша!") }
                .onBreak { player, packetPlayInBlockDig -> (player as CraftPlayer).handle.playerConnection.sendPacket(PacketPlayOutBlockChange().apply {
                    a = packetPlayInBlockDig.a
                    block = MetaWorld.storage[player.uniqueId]!!.buildings[0].allocation!!.blocks?.get(a)!!
                }) })
        return list
    }
}
```



>Если вы хотите углубиться в использование данной api, вы можете
посмотреть исходный код, там есть подробное описание каждой функции и класса.

