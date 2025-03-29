# SRF (Slime Region Format) v12 Specification

This document describes the structure of the SRF v12 file format for storing Minecraft world regions.

## 1. World Format

An SRF file begins with the following structure:

| Data Type                   | Size (Bytes) | Description                                                                                      |
|-----------------------------|--------------|--------------------------------------------------------------------------------------------------|
| `header`                    | 2            | **Header:** `0xB10B` (Identifies SRF file)                                                       |
| `version`                   | 1 (ubyte)    | **Format Version:** `0x0C` (SRF v12)                                                             |
| `worldVersion`              | 4 (int)      | **Minecraft World Version:** Integer representing the Minecraft version the region was saved in. |
| `compressedChunksSize`      | 4 (int)      | **Compressed Chunks Size:**  Size (in bytes) of the compressed chunk data (see below).           |
| `uncompressedChunksSize`    | 4 (int)      | **Uncompressed Chunks Size:**  Size (in bytes) of the uncompressed chunk data.                   |
| `chunks`                    | -            | **Chunks Data:** An array of chunk structures, compressed using Zstd.                            |
| `compressedExtraDataSize`   | 4 (int)      | **Compressed Extra Data Size:** Size (in bytes) of the compressed extra data.                    |
| `uncompressedExtraDataSize` | 4 (int)      | **Uncompressed Extra Data Size:** Size (in bytes) of the uncompressed extra data.                |
| `extraData`                 | -            | **Extra Data:** A Compound NBT tag containing any additional world data, compressed using Zstd.  |

## 2. Chunk Format

The `chunks` section from the World Format is composed of individual chunk structures. Each chunk has the following
format:

| Data Type           | Size (Bytes) | Description                                                                                                                                                                                                                            |
|---------------------|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `chunkX`            | 4 (int)      | **Chunk X Coordinate:** The X coordinate of the chunk.                                                                                                                                                                                 |
| `chunkZ`            | 4 (int)      | **Chunk Z Coordinate:** The Z coordinate of the chunk.                                                                                                                                                                                 |
| `sectionCount`      | 4 (int)      | **Section Count:** The number of sections in this chunk.                                                                                                                                                                               |
| `sections`          | -            | **Sections Data:** An array of section structures (see below).                                                                                                                                                                         |
| `heightmapsSize`    | 4 (int)      | **Heightmaps Size:** Size (in bytes) of the heightmaps NBT data.                                                                                                                                                                       |
| `heightmaps`        | -            | **Heightmaps:** A Compound NBT tag containing the heightmap data in the standard Minecraft format.                                                                                                                                     |
| `blockEntitiesSize` | 4 (int)      | **Block Entities Size:** Size (in bytes) of the block entities NBT data.                                                                                                                                                               |
| `blockEntities`     | -            | **Block Entities:** A Compound NBT tag containing a list of block entities: `{ "tileEntities": [ ... ] }`. The `tileEntities` tag is an array of Compound NBT tags, each representing a block entity in the standard Minecraft format. |
| `entitiesSize`      | 4 (int)      | **Entities Size:** Size (in bytes) of the entities NBT data.                                                                                                                                                                           |
| `entities`          | -            | **Entities:** A Compound NBT tag containing a list of entities:` { "entities": [ ... ] }`. The `entities` tag is an array of Compound NBT tags, each representing an entity in the standard Minecraft format.                          |
| `extraDataSize`     | 4 (int)      | **Extra Data Size:** Size (in bytes) of the extra data NBT data.                                                                                                                                                                       |
| `extraData`         | -            | **Extra Data:** A Compound NBT tag containing any additional chunk data.                                                                                                                                                               |

## 3. Section Format

The `sections` section from the Chunk Format is composed of individual section structures. Each section has the following format:

| Data Type         | Size (Bytes) | Description                                                                                            |
|-------------------|--------------|--------------------------------------------------------------------------------------------------------|
| `hasSkyLight`     | 1 (boolean)  | **Has Sky Light:**  `true` if sky light data is present, `false` otherwise.                            |
| `skyLight`        | 2048         | **Sky Light Data:** (Optional, present if `hasSkyLight` is `true`) 2048 bytes of sky light data.       |
| `hasBlockLight`   | 1 (boolean)  | **Has Block Light:** `true` if block light data is present, `false` otherwise.                         |
| `blockLight`      | 2048         | **Block Light Data:** (Optional, present if `hasBlockLight` is `true`) 2048 bytes of block light data. |
| `blockStatesSize` | 4 (int)      | **Block States Size:** Size (in bytes) of the block states NBT data.                                   |
| `blockStates`     | -            | **Block States:** A Compound NBT tag containing the block states data.                                 |
| `biomesSize`      | 4 (int)      | **Biomes Size:** Size (in bytes) of the biomes NBT data.                                               |
| `biomes`          | -            | **Biomes:** A Compound NBT tag containing the biome data.                                              |

## Notes:

* "Minecraft format" for NBT tags refers to the standard NBT structure used by Minecraft for storing world data.
* Boolean 1 stands for TRUE, boolean 0 stands for FALSE
