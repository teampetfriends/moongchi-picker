# moongchi-picker
[![](https://jitpack.io/v/teampetfriends/moongchi-picker.svg)](https://jitpack.io/#teampetfriends/moongchi-picker)
> this is only experimental version of MoongchiPicker. it will change a lot later.
<br/><br/>

## What is MoongchiPicker?
MoongchiPicker is custom media gallery base on Google's Material Design Bottom Sheets.
You can fetch image or video easily from camera app or gallery app or just moongchiPicker.
Also you can request permission for fetching media from storage easily. Just give **allowPermissionRequest** option when use MoongchiPicker.
MoongchiPicker support targetSdk 30 & scoped storage. And use registerForActivityResult rather than onActivityResult.
MoongchiPicker depends on only one third party library **Ucrop**.
<br/><br/>
## Setup

### Gradle

To get a project into your build:

#### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```gradle

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

Or in setting gradle

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
	...
        maven { url "https://jitpack.io" }
    }
}
```

#### Step 2. Add the dependency

```javascript
	dependencies {
	        implementation 'com.github.teampetfriends.moongchi-picker:final:1.0.1'
	}
```
<br/><br/>
## How to use

Due to MoongchiPicker use registerForActivityResult, you have to pass AppCompatActivity as argument.
And let MoongchiPicker know what media type you want to pick.
You don't need to check permission if you give **allowPermissionRequest** option true to moongchiPicker.
And pass MoongchiPickerListener as argument. That's it.

```kotlin
    val moongchiPicker = MoongchiPicker(
            this,
            mediaType = PetMediaType.IMAGE,
            allowPermissionRequest = true,
            moongchiPickerListener = object : MoongchiPickerListener{
                override fun onSubmitMedia(contentUris: List<Uri>) {
                    //do something you want to do with media
                }

                override fun onFailed(t: Throwable) {
                   
                }

            })

        binding.iv.setOnClickListener {
            moongchiPicker.show()
        }
```

If you want to make users to pick multiple media,

```kotlin
 val moongchiPicker = MoongchiPicker(
            this,
            mediaType = PetMediaType.IMAGE,
            allowPermissionRequest = true,
            allowMultiple = true,
            maxMediaCountBuilder = { 5 },
            moongchiPickerListener = object : MoongchiPickerListener{
                override fun onSubmitMedia(contentUris: List<Uri>) {
                    //do something you want to do with media
                }

                override fun onFailed(t: Throwable) {

                }

            })

        binding.iv.setOnClickListener {
            moongchiPicker.show()
        }
```

And there is callback called when user picks media from gallery if user pick items over limit that you made.

```kotlin
  override fun onSelectedMediaCountOverLimit(limit: Int) { 
  	// do something like showing warning dialog
  }
```
