# android-media-picker
A library for choose photos, videos from Android devices.

Here are some of the features of the Android Media Picker Library:

* 1) Allows you to pick one or multiple photos or videos
* 2) Options to crop photo.
* 3) Options for pick video.
* 4) Use with Activity or Fragment.


## Code

### Single image and select Cut

```Java
MediaOptions.Builder builders = new MediaOptions.Builder();
        MediaOptions options =
            builders.setIsCropped(true).setFixAspectRatio(true).setImageSize(1).build();
        MediaPickerActivity.open(this, REQUEST_MEDIA, options, false);
```
### Select multiple pictures

```Java
  MediaOptions.Builder builder = new MediaOptions.Builder();
        MediaOptions options1 =
            builder.canSelectMultiPhoto(true).setMediaListSelected(items).setImageSize(9).build();
        MediaPickerActivity.open(this, REQUEST_MEDIA, options1, false);
```
### Photographed and cut

```Java
MediaOptions.Builder builders2 = new MediaOptions.Builder();
        MediaOptions options2 =
            builders2.setIsCropped(true).setFixAspectRatio(true).setImageSize(1).build();
        MediaPickerActivity.open(this, REQUEST_PHOTOGRAPH, options2, true);
```

## How do I use it?

### Maven
```xml
<dependency>
  <groupId>com.kevinbas</groupId>
  <artifactId>android-media-picker</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```
### Gradle
```xml
compile 'com.kevinbas:android-media-picker:1.0.1'
```
## Note
- you must declare activity in AndroidManifest.xml

```xml
  <activity
            android:name="mediapicker.activities.MediaPickerActivity"
            android:screenOrientation="portrait"
            android:theme="@style/MediaPickerTheme"/>
```

## Update

### Publish 1.0.1

- add Android M Runtime Permission 
- fix Picker photo information

## Thanks

This project after [tungdx/android-media-picker](https://github.com/tungdx/android-media-picker) modify , add a picture of quantitative restrictions , direct camera functions Introduction

## License
Copyright 2016 jiangTaoQuite

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.


