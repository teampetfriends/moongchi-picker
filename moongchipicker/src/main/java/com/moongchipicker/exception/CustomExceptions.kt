package com.moongchipicker.util

class PermissionDeniedException(deniedPermissions : Array<String>) : Exception(deniedPermissions.toString())
class GetPictureFailedException() : Exception("[ActivityResultContracts.GetContent()] failed : result uri is null")
class GetMultiplePicturesFailedException() : Exception("[ActivityResultContracts.GetMultipleContents()] failed : result uri is null")
class GetVideoFailedException() : Exception("[ActivityResultContracts.GetContent()] failed : result uri is null")
class GetMultipleVideoFailedException() : Exception("[ActivityResultContracts.GetMultipleContents()] failed : result uri is null")

