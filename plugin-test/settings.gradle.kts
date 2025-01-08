pluginManagement {
    includeBuild("../plugin")
}
plugins {
    id("com.highlylogical.artifactrepo")
}

//if (pluginManager.hasPlugin("com.highlylogical.artifactrepo")) {
//    println("The settings plugin is applied!")
//} else {
//    println("The settings plugin is not applied!")
//}