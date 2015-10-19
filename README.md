# Growth Analytics SDK for Android

:rotating_light: **This SDK is no longer maintained** :rotating_light:  
You can use Growthbeat sdk insead:
* [Growthbeat SDK for iOS](https://github.com/SIROK/growthbeat-ios/)
* [Growthbeat SDK for Android](https://github.com/SIROK/growthbeat-android/)

---

[Growth Analytics](https://analytics.growthbeat.com/) is analytics service for mobile apps.

## Usage 

1. Install [Growthbeat Core SDK](https://github.com/SIROK/growthbeat-core-android).

1. Add growthanalytics.jar into libs directory in your project.

1. Write initialization code in MainActivity's onCreate.

	```java
	GrowthAnalytics.getInstance().initialize(getApplicationContext(), "APPLICATION_ID", "CREDENTIAL_ID");
	```

1. Write following code in MainActivity's onStart

	```java
	GrowthAnalytics.getInstance().open();
	```

1. Write following code in MainActivity's onStop

	```java
	GrowthAnalytics.getInstance().close();
	```

## Growthbeat Full SDK

You can use Growthbeat SDK instead of this SDK. Growthbeat is growth hack tool for mobile apps. You can use full functions include Growth Push when you use the following SDK.

* [Growthbeat SDK for iOS](https://github.com/SIROK/growthbeat-ios/)
* [Growthbeat SDK for Android](https://github.com/SIROK/growthbeat-android/)

## License

Apache License, Version 2.0
