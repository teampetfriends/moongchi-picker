package com.moongchipicker.util

import java.lang.Exception

class PermissionDeniedException(deniedPermissions : Array<String>) : Exception(deniedPermissions.toString())
class GetPictureFailedException() : Exception("[ActivityResultContracts.GetContent()] failed : result uri is null")
class GetMultiplePicturesFailedException() : Exception("[ActivityResultContracts.GetMultipleContents()] failed : result uri is null")
class GetVideoFailedException() : Exception("[ActivityResultContracts.GetContent()] failed : result uri is null")
class GetMultipleVideoFailedException() : Exception("[ActivityResultContracts.GetMultipleContents()] failed : result uri is null")
class TakePictureFailedException() : Exception("[ActivityResultContracts.TakePicture()] failed : picture not saved")
class TakeVideoFailedException() : Exception("[TakeVideoContract()] failed : result code is not Activity.RESULT_OK")
