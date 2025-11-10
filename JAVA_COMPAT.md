# Meergo Kotlin SDK Java Compatibility 

The Meergo Kotlin SDK lets you send customer event data from your Android applications to your specified destinations.

NOTE: This document serves as an explanation of usage of this `analytics-kotlin` library for pure Java codebase. For the sample usages in Kotlin, please refer to our main [README.md doc](README.md).

## SDK setup requirements

- Set up a Meergo account.
- Set up an Android source in the dashboard.
- Copy the write key and the endpoint.

## Installation

To integrate the Android SDK inside your application, add the dependency to your `build.gradle`. Make sure to replace `<latest_version>` with the latest version of the SDK.

```java
repositories {
  mavenCentral()
}
dependencies {
  implementation 'com.meergo.analytics.kotlin:android:<latest_version>'
}
```

Add the required permissions to `AndroidManifest.xml` (if they are not yet present).
```xml
<!-- Required for internet. -->
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
``` 
  
## Using the SDK

```Java
AndroidAnalytics client = AndroidAnalyticsKt.Analytics(BuildConfig.YOUR_WRITE_KEY, getApplicationContext(), configuration -> {
  configuration.setEndpoint(BuildConfig.YOUR_ENDPOINT);
  configuration.setFlushAt(1);
  configuration.setCollectDeviceId(true);
  configuration.setTrackApplicationLifecycleEvents(true);
  configuration.setTrackDeepLinks(true);
  //...other config options
  return Unit.INSTANCE;
});

JavaAnalytics analyticsCompat = new JavaAnalytics(client);
```

## Sending events

Refer to the Meergo events documentation for more information on the supported event types.

## License
```
MIT License

Copyright (c) 2024 Open2b
Copyright (c) 2021 Segment

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
