/*
 * Copyright (C) 2009 Virgil Dobjanschi, Jeff Sharkey, Filip Maelbrancke
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.maelbrancke.filip.devoxx.schedule.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AppUtil {
    /**
     * Show a level
     * 
     * @param context The context
     * @param level The level
     */
    public static void showLevel( Context context, int level)
    {
        String filename;
        switch( level)
        {
            case 1:
            {
                filename = "ground_floor.png";
                break;
            }
            case 2:
            {
                filename = "talks_floor.png";
                break;
            }
            
            default:
            {
                return;
            }
        }
        File f = new File( context.getFilesDir() + "/" + filename);
        try {
            if( f.exists() == false) {
                InputStream is = context.getAssets().open( filename);
                FileOutputStream fos = context.openFileOutput( filename, Context.MODE_WORLD_READABLE);
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                // write the stream to file
                fos.write(buffer, 0, buffer.length);
                fos.close();
                is.close();
            }

            // Prepare the intent
            Intent intent = new Intent( Intent.ACTION_VIEW );
            intent.setDataAndType(Uri.fromFile( f), "image/png");
            context.startActivity(intent);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
