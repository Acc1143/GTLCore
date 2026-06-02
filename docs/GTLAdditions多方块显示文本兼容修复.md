# GTLAdditions 多方块显示文本兼容修复

这不是事故流水账，是以后别再被同一个坑绊倒的说明。

## 这破事怎么触发

当前重打包的 GTLCore 和 `gtladditions-1.0.5-build_6.jar` 一起塞进整合包以后，右键某些大型电力多方块会在打开 UI 的时候崩。

已经踩过的地方：

- `gtceu` / GTL 注册的 `WorkableElectricMultiblockMachine` 多方块。
- 大型筛选漏斗。
- 裂变反应堆。

真正炸的位置不是方块结构，也不是配方逻辑，而是 UI 显示文本：

```text
WorkableElectricMultiblockMachine.addDisplayText(List<Component>)
```

典型异常长这样：

```text
java.lang.IncompatibleClassChangeError:
Method 'void com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine.addDisplayText(java.util.List)'
must be Methodref constant
```

堆栈里一般还能看到这些东西一起出现：

- `gtlcore.mixin.json:gtm.fix.WorkableElectricMultiblockMachineMixin`
- `gtladditions.mixin.json:gtceu.WorkableElectricMultiblockMachineMixin`
- `WorkableElectricMultiblockMachine.handler$...$addDisplayText`

## 锅在哪

`gtladditions` 的 `WorkableElectricMultiblockMachineMixin` 往 `addDisplayText` 里塞了一段逻辑，最后调用了这个：

```java
IDisplayUIMachine.super.addDisplayText(textList);
```

普通 Java 类里这么调接口默认方法没什么稀奇，问题是这玩意儿在 Mixin 注入方法里。Mixin 会把注入方法复制、改名，再合并到目标类里。等 JVM 真跑到这条 `invokespecial` 指令时，常量池引用形态已经和目标方法对不上，于是直接抛 `IncompatibleClassChangeError`。

所以它不是启动就炸。类能加载，游戏也能进，直到右键打开多方块 UI，真正跑到显示文本 handler 才炸。

## 这次没干什么

这些做法都没用：

- 没直接改 `gtladditions-1.0.5-build_6.jar`。
- 没禁用整个 `gtladditions` mixin。
- 没把 `gtladditions` 做成 GTLCore 的编译期依赖。
- 没继续在 `gtlcore.mixin.json` 里乱删别的功能 mixin。

说白了，不靠拆东墙补西墙，也不靠手改 jar 装死。

## 实际怎么修

修复位置：

```text
src/main/java/org/gtlcore/gtlcore/mixin/gtm/fix/WorkableElectricMultiblockMachineMixin.java
```

现在的做法：

- 把这个 mixin 的优先级设成 `500`，低于 `gtladditions` 默认优先级。
- 在 GTLCore 侧 `@Overwrite` `WorkableElectricMultiblockMachine.addDisplayText(List<Component>)`。
- 由 GTLCore 重新生成显示文本，让 `gtladditions` 那个坏 handler 变成不可达。
- 运行时如果找得到 `gtladditions` 的 `GTLAddMultiblockDisplayTextBuilder`，就反射调用它，保留重力、洁净室等级这些额外显示行。
- 如果反射找不到或方法对不上，就回退到 GTCEu 原始 `MultiblockDisplayText`。
- 最后手动遍历 `IMultiPart`，调用 `part.addMultiText(textList)`，替掉危险的 `IDisplayUIMachine.super.addDisplayText(textList)`。

流程就是：

```text
GTLCore overwrite addDisplayText
  -> 计算并行数
  -> 优先复用 GTLAdditions builder
  -> 失败就用 GTCEu 原始 builder
  -> 执行 machine.getDefinition().getAdditionalDisplay()
  -> 手动追加每个部件的 addMultiText()
```

## 这修复靠什么前提活着

它靠两个前提：

- `gtladditions` 的坏调用还在 `WorkableElectricMultiblockMachine.addDisplayText` 这条注入链路里。
- 低优先级 `@Overwrite` 能在目标方法最终合并时盖掉注入入口。

如果以后 `gtladditions` 更新，把 builder 包名、类名或者方法签名挪了，GTLCore 会回退到 GTCEu 原始显示文本。这样不至于又因为反射失败崩，但 `gtladditions` 那几行额外显示可能会没了。

当前已验证：右键大型筛选漏斗、裂变反应堆等大型电力多方块，不再因为 `addDisplayText` 崩溃。
