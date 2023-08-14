> [![](https://jitpack.io/v/sieunju/httptracking.svg)](https://jitpack.io/#sieunju/httptracking)   
> 앱 개발시 Log.d 보지 않고 BottomSheetDialog 로 간단하게 볼수 있는 개발용 라이브러리입니다.
---
![AndroidMinSdkVersion](https://img.shields.io/badge/minSdkVersion-21-green.svg) ![AndroidTargetSdkVersion](https://img.shields.io/badge/targetSdkVersion-32-brightgreen.svg)

안드로이드 HTTP 통신을 Netty로 사용한다면...빠르게 뒤로가기를 눌러주세요 😭

이 라이브러리는 OkHttp3 기준으로 동작합니다.

> 앱을 사용하다가 단말기를 흔들면 트래킹한 로그들을 간단히 볼수 있습니다.

## 라이브러리 추가 하는 방법

- Project Gradle (kts)

```groovy
allprojects {
    repositories {
        maven { url = URI("https://jitpack.io") }
    }
}
```

- App Module Gradle

```groovy
dependencies {
    // UI
    implementation("com.github.sieunju.httptracking:ui:$latestVersion")
    // Interceptor Java Module
    implementation("com.github.sieunju.httptracking:interceptor:$latestVersion")
}
```

## 유의사항
- 혹시나 머티리얼을 사용하시거나 프로젝트에 사용중인 라이브러리랑 충돌이 일어나는 경우에는 아래와 같이 사용해주시면 됩니다. 🙇‍♂️
- A.K.A exclude
```groovy

implementation("com.github.sieunju.httptracking:ui:${lateversion}") {
    exclude("com.google.android.material")
    exclude("androidx.appcompat:appcompat")
    exclude("androidx.constraintlayout")
}
```

## 사용방법

Builder 패턴으로 Application Class 에서 간단히 빌드 타입을 설정합니다.

```kotlin
Application.kt

HttpTracking.Builder()
    .setBuildType(BuildConfig.DEBUG)
    .setLogMaxSize(3000)
    .build(this)
```

OkHttpClient 에서 ‘addInterceptor’ 를 통해 TrackingHttpInterceptor 를 추가합니다.

해당 라이브러리는 되도록이면 디버그모드에만 처리하도록 지향합니다.

```kotlin
OkHttpClient.Builder().apply{
    if(Debug) {
        addInterceptor(TrackingHttpInterceptor)
    }
}
```

|UI Example|
|:--:|
|![UI](https://raw.githubusercontent.com/sieunju/httptracking/develop/storage/list_example_1.png)|

## PC로 로그 확인 하는 방법

setWifiShare(true)
```kotlin
HttpTracking.Builder()
    .setBuildType(BuildConfig.DEBUG)
    .setWifiShare(true)
    .setLogMaxSize(3000)
    .build(this)
```
|Step 1|Step 2|Step 3|
|--|--|--|
|![Step1](https://raw.githubusercontent.com/sieunju/httptracking/develop/storage/example_wifi_share_1.png)|![Step2](https://raw.githubusercontent.com/sieunju/httptracking/develop/storage/example_wifi_share_2.png)|![Step3](https://raw.githubusercontent.com/sieunju/httptracking/develop/storage/example_wifi_share_3.png)|

위에 화면처럼 원하는 로그를 선택하시고 공유 하기 버튼을 선택하시면 http://{ip}:{port}/tracking 주소가 노출되는데 해당 주소를 PC에 입력하시면 선택한 로그를 볼수 있습니다. 🤩

#### 🙏 공공장소에서 사용은 절대로 지양합니다. (본인의 HTTP 통신 로그를 누군가 볼수 있습니다..)

