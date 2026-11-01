# Arachnid Guard / 蛛类屏蔽

[English](#english) | [简体中文](#简体中文)

## English

Arachnid Guard is a Minecraft mod for players who prefer to avoid spiders and selected modded arthropods. It blocks targeted entities across dimensions, filters naturally generated webs, and removes targeted natural spawners while preserving supported player-placed blocks.

**Development credit:** The program code, tests, and documentation were written and organized by OpenAI Codex based on the project initiator's requirements. The initiator defined and confirmed the requirements and shares the project with the community. This is an independent, AI-assisted community project, not an official OpenAI, Mojang, Forge, NeoForge, or Better MC product.

### Downloads and installation

| Minecraft | Loader and Java (build/test versions) | Download |
|---|---|---|
| 1.20.1 | Forge 47.4.20 / Java 17 | [Forge JAR](downloads/arachnidguard-forge-1.20.1-1.0.0.jar) |
| 1.21.1 | NeoForge 21.1.250 / Java 21 | [NeoForge JAR](downloads/arachnidguard-neoforge-1.21.1-1.0.0.jar) |

Open the matching file above and use GitHub's download button. Close Minecraft, place the JAR in your instance's `mods` folder, then restart the game. Choose the version matching your Minecraft version and loader; do not install both. No additional dependency mods are required. For multiplayer, install it on the server; a client-only installation cannot control a remote server's spawning.

### Features

- Blocks vanilla spiders and cave spiders, plus configured modded spiders, centipedes, scorpion-like creatures, beetles, mosquitoes, cockroaches, mechanical spiders, and other selected targets.
- Covers natural spawning, spawners, commands, spawn eggs, and loading saved entities. Rules have no dimension or player-distance restriction.
- Keeps vanilla bees, silverfish, and endermites, as well as creatures such as ordinary crabs and butterflies that are not targeted by the rules.
- Filters known natural webs during world generation and block placement. Existing natural webs are cleaned when their chunks load.
- Removes natural spawners containing targeted entities while retaining unrelated spawners. The NeoForge edition also handles normal and ominous trial spawners.
- Records and preserves webs, nests, and spawners placed through normal player placement. Protection is saved with the chunk. Preserved spawners still cannot spawn blocked entities.

The defaults were checked against the Better MC BMC4/BMC5 installations used during development. Known targets include creatures from Alex's Mobs, The Twilight Forest, Cataclysm, Deeper and Darker, Hybrid Aquatic, and other mods. The NeoForge edition also includes Deep Aether's Venomite and Windfly. Mechanical spiders from the Delta Force skills add-on are included. None of these content mods is required to use Arachnid Guard.

### Important limitations

- Installing before creating a new world is recommended. Player-placed webs from before installation have no origin record and may be removed as natural webs. Back up existing worlds first.
- The mod does not scan every unloaded chunk on disk at once. Rules apply to old chunks wherever they are when those chunks load.
- Keep the mod enabled to continue blocking future generation and spawning.
- Spider cave walls, ordinary structures, loot, and decorative blocks that merely depict spiders are not removed.
- Placement by commands, WorldEdit, machines, or special tools bypassing normal `BlockItem` placement is not treated as ordinary player placement.
- This is not automatic recognition of every arthropod in every mod. Detection uses configured IDs, selected entity-ID keywords, and inheritance from `Spider`. Unrecognized creatures may require additional IDs in the configuration.
- Blocking creatures or bosses may affect quests, progression, and access to drops.

### Configuration and commands

After the first run, edit `config/arachnidguard-common.toml` and restart the game or server.

| Setting | Purpose |
|---|---|
| `blockedEntities` | Exact entity IDs to block |
| `allowedEntities` | Explicitly allowed IDs; takes highest priority |
| `moddedEntityKeywords` | Keywords matched against modded entity IDs only |
| `naturalWebBlocks` | IDs of natural web, egg, and nest blocks to remove |
| `removeNaturalWebs` | Enable or disable natural web removal |
| `removeTargetSpawners` | Enable or disable targeted spawner removal |

Administrators can use `/arachnidguard list` to inspect matching entities and `/arachnidguard status` for status and counters from the current session. Counters do not include every world-generation filtering operation.

### Testing scope

The NeoForge 1.21.1 edition passed 13 functional tests on an isolated test server, covering vanilla spider blocking, preservation of other vanilla arthropods, generation filtering, existing web cleanup, player-placement protection, and regular/trial spawners.

Tests covered 28 modded target entity types and 11 web block variants, with relevant mods including TaCZ, the Delta Force skills add-on, C2ME, Lithium, and ModernFix. A real server save, shutdown, and restart with optimization mods verified that a player-placed web and its protection marker persisted. Cross-dimension tests covered the Overworld, Nether, and End.

These results do not guarantee compatibility with every complete modpack, modded dimension, or future version. Extended playtesting of the full BMC5 client has not been completed. When reporting an issue, include the affected entity or block ID, Minecraft/loader versions, and relevant logs with personal information removed.

### Building from source

Enter `forge-1.20.1` with JDK 17 or `neoforge-1.21.1` with JDK 21, then run `./gradlew build` (`gradlew.bat build` on Windows). Built JARs appear in that edition's `build/libs` directory.

GameTest sources are included; use `runGameTestServer` for the base tests. Compatibility tests require you to supply the relevant third-party mods and dependencies, which are not distributed in this repository or the release JARs.

### License

Released under the [MIT License](LICENSE). Minecraft, loader, and third-party mod names and trademarks belong to their respective owners.

---

## 简体中文

为不想在 Minecraft 中遇见蜘蛛及指定节肢生物的玩家制作的小模组。支持全维度拦截目标实体，过滤自然蜘蛛网，并清理目标刷怪笼，同时保留正常玩家手放的蜘蛛网和刷怪笼。

**开发说明：本项目的程序代码、测试和文档由 OpenAI Codex 根据项目发起者提出的需求编写与整理，发起者负责需求确认与分享。项目使用了 AI 辅助开发，属于个人社区项目，不是 OpenAI、Mojang、NeoForge、Forge 或 Better MC 官方产品。**

## 下载

| Minecraft | 加载器（构建和测试版本） | 下载 |
|---|---|---|
| 1.20.1 | Forge 47.4.20 / Java 17 | [Forge 版 JAR](downloads/arachnidguard-forge-1.20.1-1.0.0.jar) |
| 1.21.1 | NeoForge 21.1.250 / Java 21 | [NeoForge 版 JAR](downloads/arachnidguard-neoforge-1.21.1-1.0.0.jar) |

点击对应文件后使用 GitHub 的下载按钮。退出游戏，将 JAR 放入该实例的 `mods` 文件夹，再启动游戏。两个版本不能混用；不需要额外前置。多人游戏需要服务端安装，只在客户端安装不能控制远程服务器的生成。

## 功能

- 阻止原版蜘蛛、洞穴蜘蛛，以及名单内的模组蜘蛛、蜈蚣、蝎类、甲虫、蚊虫、蟑螂及机械蜘蛛等实体进入世界。
- 覆盖自然刷怪、刷怪笼、命令、刷怪蛋及旧实体加载。规则不限制维度或玩家距离。
- 保留原版蜜蜂、蠹虫、末影螨，以及未列入名单的普通螃蟹、蝴蝶等生物。
- 在世界生成和方块写入时过滤已知自然蜘蛛网；旧区块中的自然网在加载后清理。
- 删除包含目标实体的自然刷怪笼，其他刷怪笼保留。NeoForge 版还处理普通和不祥试炼刷怪笼。
- 正常玩家手放的网、巢和刷怪笼会记录来源并保留，保护记录随区块保存。保留的刷怪笼仍无法生成目标生物。

默认名单根据开发时的 Better MC BMC4/BMC5 安装内容核查，包含 Alex's Mobs、暮色森林、灾变、Deeper and Darker、Hybrid Aquatic 等已知目标；NeoForge 版额外包含 Deep Aether 的 Venomite、Windfly。也包含三角洲技能中的机械蜘蛛。未安装这些模组不影响本模组独立运行。

## 使用前请了解

- 推荐在创建新存档前安装。安装前的人工蜘蛛网没有来源记录，无法可靠区分，可能被当作自然网清理；已有存档请先备份。
- 不会一次性扫描磁盘上的整张旧地图；旧区块无论在哪里，加载后都会应用规则。
- 需要保持模组启用；卸载后不再限制后续生成。
- 不删除蜘蛛洞穴岩壁、普通建筑和战利品，也不删除仅带蜘蛛图案的装饰方块。
- 命令、WorldEdit、机器或绕过普通 BlockItem 流程的特殊工具放置，不等同于正常手放。
- 并非自动识别世界上所有节肢生物。使用名单、部分实体 ID 关键词与 Spider 继承关系识别；新模组的特殊生物可能需要补充 ID。
- 禁用生物或首领可能影响任务、攻略及掉落获取。

## 配置

首次运行后生成 `config/arachnidguard-common.toml`，修改后重启游戏。

| 配置项 | 用途 |
|---|---|
| `blockedEntities` | 精确屏蔽实体 ID |
| `allowedEntities` | 明确放行 ID，优先级最高 |
| `moddedEntityKeywords` | 仅对模组实体 ID 生效的匹配关键词 |
| `naturalWebBlocks` | 要清理的自然网、卵、巢方块 ID |
| `removeNaturalWebs` | 自然网清理开关 |
| `removeTargetSpawners` | 目标刷怪笼清理开关 |

管理员可使用 `/arachnidguard list` 查看当前匹配名单，使用 `/arachnidguard status` 查看状态和本次运行计数。计数不包含所有生成阶段的过滤动作。

## 验证范围

NeoForge 1.21.1 版在独立测试服务器中通过 13 项功能测试，覆盖原版蜘蛛拦截、其他原版节肢生物保留、世界生成过滤、旧网清理、手放保护与两种刷怪笼。

实际测试了 28 种模组目标实体、11 种蜘蛛网方块，并与 TaCZ、三角洲技能、C2ME、Lithium、ModernFix 等相关模组共同运行测试。优化模组环境下的真实关服保存及重启，也验证了手放网及来源标记保留。跨维度测试实际覆盖主世界、下界和末地。

上述结果不代表完整整合包、每个模组维度或后续版本均无冲突。未完成完整 BMC5 客户端的长时间游玩验证。欢迎提交具体实体 ID、Minecraft/加载器版本及必要错误日志，提交前请移除日志中的个人信息。

## 从源码构建

进入对应版本目录，设置相应 JDK 后运行：

```sh
./gradlew build
```

Windows 使用 `gradlew.bat build`。产物位于该版本目录的 `build/libs`。源码附带 GameTest；`runGameTestServer` 可运行基础测试。兼容测试需要自行准备第三方模组和前置，它们不包含在仓库或发布 JAR 中。

## 许可

项目按 [MIT License](LICENSE) 开源。Minecraft、加载器及其他模组的名称与商标归各自权利人所有。
