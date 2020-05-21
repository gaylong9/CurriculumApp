# CurriculumApp

课程作业

## 1.3ver

* 修改配色
* 增加上课时间
* 细节优化



## 2.0ver

新增周数记录功能

记录总周数、本学期哪年开始、本学期在该年的第几周开始。

今年开始：当前学期周 = 当前自然周 - 学期开始周 + 1

去年开始：当前学期周 = 去年总周数 - 学期开始周 + 1 + 当前自然周



## 3.0ver

1. 课程卡片调整：增加两个图标入口
	1. 设置：代替以前的点击卡片
	2. 便签：添加作业/计划（待办），
2. 新增便签功能：
	1. 文字/待办：
		1. 可点击○表示完成，图标会变化，对应文本会出现删除线：ImageView+EditText，``setPaintFlags`
		2. 回车，当前项后插入新项：获取当前焦点，数个``getParent()``获得整个文字项（此处需要强制转换），``父布局.addView(新项, index+1)``
		3. 删除：监听删除键，判断光标处于最左，获取位置index后`removeView()`，之后上方的文字项获得焦点	
	2. 录音：
		1. 录音，点击录音图标，开始录音，再次点击结束录音。在当前焦点之后，或整个便签末尾增加一个录音项：以时间作为文件名，生成录音文件（`createNewFile()`前要先``mkdirs()``一个新的目录，``createTempFile()``会在文件名后缀一串随机数以防文件重复/已存在，故采用``createNewFile()``）；使用MediaRecord录音，设置顺序很重要
		2. 播放，点击录音项里的声音图标，开始播放；再次点击，暂停播放：使用MediaRecord播放与暂停
	3. 图片
		1. 点击图片键，启动相册，选择图片，生成图片项：使用intent启动相册；在当前焦点或末尾增加图片项
		2. 点击放大图标，图片放大，再次点击缩小：图片项内含两个ImageView，随着点击，两个交替GONE与VISIBLE
3. 数据库调整：删除TimeInfo表，并入Settings表
4. 细节优化



## 数据库

### Curriculum表：

1. id：主键，自增；三位，星期（1）+当天课程序号（2）
2. teacher：text，记录教师名
3. location：text，记录教室
4. name：text，记录课程名

### Notes表：

1. id：主键，自增
2. course_id：以课程号分类
3. type：note、record、image 以三种便签内容分类
4. content：edittext、path、path 分别对应的内容
5. 数据库2.0ver：加入Notes表

### Settings表：

1. 组成
	1. id：主键自增
	2. type: text
	3. keys: text
	4. value: text
2. 周数存储：
	1. type: week_num	
	2. 总周数：keys = week_sum 
	3. 当前周：用于计算当前是本学期第几周
		1. keys:start_year 本学期哪年开始
		2. keys:start_week_of_year 本学期在该年的第几周开始
		3. 今年开始：当前学期周 = 当前自然周 - 学期开始周 + 1
		4. 去年开始，当前学期周 = 去年总周数 - 学期开始周 + 1 + 当前自然周
3. 时间存储：库2.0ver中删除TImeInfo表，并入Settings表。
	1. keys：time+i
	2. type: start_time



