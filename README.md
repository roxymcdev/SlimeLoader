# SlimeLoader

A simple Minecraft Slime world format (originally described in [Hypixel Dev Blog #5](https://hypixel.net/threads/dev-blog-5-storing-your-skyblock-island.2190753/)) loader.

This loader is based on the Slime format specification from [InfernalSuite/AdvancedSlimePaper](https://github.com/InfernalSuite/AdvancedSlimePaper/blob/main/SLIME_FORMAT).

## Supported Slime versions

| Slime version | Compatible loader version |
|---------------|---------------------------|
| 12            | 1.0                       |

For older versions see [Legacy Slime version](#legacy-slime-versions) section.

## Legacy Slime versions

Originally, our loader has been created for Slime format version 12 and there is no reason to support older format versions.

If you want to convert your legacy worlds, we advise you to use [InfernalSuite/AdvancedSlimePaper](https://github.com/InfernalSuite/AdvancedSlimePaper/) for that.

## Compatibility with other loaders

> [!NOTE]  
> The output from our loader might be slightly different from the other ones.

Our loader is fully compatible with other loaders following the same specifications.

If you encounter any issues with loading worlds serialized by other loaders, please create an [issue](https://github.com/roxymc-net/SlimeLoader/issues/) immediately!

## Usage

SlimeLoader is available in our maven repository.

Depending on your target loader version, you would need:

```kts
repositories {
    // for releases
    maven("https://repo.roxymc.net/releases")

    // for snapshots
    maven("https://repo.roxymc.net/snapshots")
}
```

and

```kts
dependencies {
    implementation("net.roxymc:slimeloader:VERSION")
}
```

## Compiling

To compile, navigate to project root directory and run:

```shell
./gradlew assemble
```
