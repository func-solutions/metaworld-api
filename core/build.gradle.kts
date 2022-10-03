dependencies {
    compileOnly("cristalix:bukkit-core:21.01.30")
    compileOnly("cristalix:dark-paper:21.02.03")
    compileOnly("me.func:world-api:1.0.7")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "metaworld-api"
            from(components["java"])
        }
    }
}