## ColorPicker
An android colorpicker with changeable color drawable for easy styling.

### Overview
=======================

This is a material designed **ColorPicker**.

The main advantage is that the color is selected from the drawable itself.

The colors are not generated, but provide through the xml layout.

This allows for very easy styling.

The radius and thumb image can be changed via xml for easy tweaking.


### Example
=======================
![](/video_example.gif)
### Usage
=======================

Add a basic colorpicker to your layout.

Set the wheelDrawable thumbDrawable and radiusOffset and thats it !

```xml
        <com.christophesmet.android.views.colorpicker.ColorPickerView
            android:id="@+id/colorpicker"
            app:wheelDrawable="@drawable/img_wheel"
            app:thumbDrawable="@drawable/img_wheel_handle"
            app:radiusOffset="24dp"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"/>
```
Attach the colorListener
```java
        mColorPickerView.setColorListener(new ColorPickerView.ColorListener() {
            @Override
            public void onColorSelected(int color) {
                mColorView.setBackgroundColor(color);
            }
        });
```

###Tweaking
=======================
If you change the wheel color drawable, then you'll probably want to change the radius offset.
To get a better view, enable the debug drawing as followed.

```java
        //Set this to true, to enable visual debugging. To check the offset radius
        mColorPickerView.setDrawDebug(false);
```
![](/debug.png)

### Adding to gradle dependencies
=======================
Add the repo:
```groovy
 repositories {
        // ...
        maven { url "https://jitpack.io" }
 }
```
Add the dependency:
```groovy
dependencies {
	        compile 'com.github.christophesmet:colorpicker:d84191b1cf'
	}
```


