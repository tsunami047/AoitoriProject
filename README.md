![图片](https://github.com/tsunami047/AoitoriProject/assets/134914438/8045d350-31be-4c83-bbf3-9042cfb90852)

青鸟Project
=======
<br>这是一个用来减少Minecraft Bukkit Plugin重复劳动的项目，包含了如下功能
1. [针对MC后端开发的持久化组件](https://github.com/tsunami047/AoitoriProject/wiki/%E6%8C%81%E4%B9%85%E5%8C%96)：使用了caffeine、redis、mysql作为缓存/存储数据库，reflectasm作为反射库、jeromq作为消息中间件，可以定义实体、定义实体关系、充分发挥关系数据模型优势，使用同步缓存、异步读写、玩家独占数据等优化手段提高读写效率，提高开发效率。
2. 指令管理组件：自动生成帮助消息，Tab补全，提高代码可读性，通过注解轻松定义指令执行权限等。
3. 配置映射组件：只需要定义配置文件实体，AoitoriProject会自动从您的插件中读取对应值到对应成员，避免了繁琐的配置set操作，可以读取/释放一个目录下的所有文件映射成对应Bean。
4. op组件：通过反射NMS实现的OP权限给予、移除，避免了原本的setOP存在读写硬盘IO问题，卡OP的风险，同时又提高了效率。
5. tuershen的nbt组件：修复了若干bug，一款功能强大的操作nbtlib。


鸣谢
--------
@ArgonarioD 指导了我持久化组件的开发
