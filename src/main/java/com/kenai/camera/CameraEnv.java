package com.kenai.camera;

import java.io.File;
import android.os.Environment;

public class CameraEnv{
	public static File getOutputFile(){
		return new File(
				Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"kenai");
		
	}
}
