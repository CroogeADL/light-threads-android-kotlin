# LightThreads for Android
LightTreads is designed to simplify using of threads in Android.  Enjoy using of Threads in your project!

## How to

To get **LightThreads** into your project:

### Step 1

Add it in your root `build.gradle` at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2

Add the dependency:
```
dependencies {
    ...
    implementation 'com.github.CroogeADL:light-threads-android-kotlin:1.0.0'
}
```

### Step 3

Use methods of `LightThreads` and Enjoy!

```kotlin
runInForeground {
    Toast.makeText(
        this@MainActivity,
        "with / without delay" + "Enjoy using LightThreads!", Toast.LENGTH_SHORT
    ).show()
}
```

```kotlin
runInBackground {
    Log.i("with / without delay", "Enjoy using LightThreads!")
}
```

```kotlin
schedule(5, TimeUnit.SECONDS) {
    Log.i("scheduled / periodically", "Enjoy using LightThreads!")
}
```

## License

The **LightThreads** is Open Source and available under the MIT license. See the LICENSE file for more information.