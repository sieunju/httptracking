> [![](https://jitpack.io/v/sieunju/httptracking.svg)](https://jitpack.io/#sieunju/httptracking)   
> 앱 개발시 Log.d 보지 않고 BottomSheetDialog 로 간단하게 볼수 있는 개발용 라이브러리입니다.
---
![AndroidMinSdkVersion](https://img.shields.io/badge/minSdkVersion-21-green.svg) ![AndroidTargetSdkVersion](https://img.shields.io/badge/targetSdkVersion-31-brightgreen.svg)

안드로이드 HTTP 통신을 Netty로 사용한다면...빠르게 뒤로가기를 눌러주세요 😭

이 라이브러리는 Retrofit2 기준으로 동작합니다.

> 앱을 사용하다가 단말기를 흔들면 트래킹한 로그들을 간단히 볼수 있습니다.

## 라이브러리 추가 하는 방법

- Project Gradle (kts)

```groovy
allprojects {
	    repositories {
		    ...
		    maven { url = URI("https://jitpack.io") }
	    }
}
```

- App Module Gradle

```groovy
dependencies {
    	implementation("com.github.sieunju:httptracking:$latestVersion")
}
```

## 사용방법

Builder 패턴으로 Application Class 에서 간단히 빌드 타입을 설정합니다.

```kotlin
Application.kt

TrackingManager.getInstance()
            .setBuildType(isDebug)
            .setLogMaxSize(1000)
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

추가적으로 Query, Path, Body 를 꾸욱 누르면 복사가 가능합니다 🥰

(공유하기 기능이나 맥북으로 보낼수 있는 방법을 생각해보도록 하겠습니다)
