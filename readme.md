# 实现功能

## 1.绘制表盘数字

## 2.秒针、分针、时针跳动

通过Timertask实现每秒一次跳动秒针，同时分针时针联动，并invalidate()重新绘制；

## 3.绘制秒针

## 4.添加与石英钟联动的数字式时钟

## 5.实现了用手拖动指针尖端调节时间的功能

### 通过重写onTouchEvent：

在ACTION_DOWN时读取触摸位置，判断手指是否在指针尖端，以及触摸的是哪根指针；

在ACTION_MOVE时计算手指触摸位置与表盘中心连线的角度，换算为时间，赋值给对应指针的时间，并invalidate()；

### 通过在activity_clock中添加两个按钮并在ClockActivity中设置监听事件：

按钮“SYNC_WITH_SYSTEM_TIME”会将时钟时间设定为系统当前时间；

按钮“SWITCH_BETWEEN_12H/24H”会将时间在小于12小时和大于12小时间切换，以克服拖动指针无法将时间设置为13点及以后的问题；


## 备注：

1.ClockView被重命名为了ClockViewTouchable

2.Touch事件参考了https://blog.csdn.net/qq1271396448/article/details/82784315