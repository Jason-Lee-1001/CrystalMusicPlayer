/*
 * Copyright (C) 2014 Saravan Pantham
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.studio.jason.application.broadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.studio.jason.application.service.MediaPlayBackService;

/**
 * BroadcastReceiver that handles and processes all headset
 * button clicks/events.
 *
 * @author Saravan Pantham
 */
public class HeadsetButtonsReceiver extends BroadcastReceiver {

    public HeadsetButtonsReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        int keycode = event.getKeyCode();
        System.out.println("keycode:" + keycode);
        int action = event.getAction();

        if (keycode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                || keycode == KeyEvent.KEYCODE_HEADSETHOOK
                || keycode == KeyEvent.KEYCODE_MEDIA_PLAY
                || keycode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
            System.out.println("------play or pause media button receive-----");
            if (action == KeyEvent.ACTION_DOWN) {
                context.sendBroadcast(new Intent(MediaPlayBackService.ACTION_PLAY));
            }
        }

        if (keycode == KeyEvent.KEYCODE_MEDIA_NEXT) {
            System.out.println("------next media button receive-----");
            if (action == KeyEvent.ACTION_DOWN) {
                context.sendBroadcast(new Intent(MediaPlayBackService.ACTION_NEXT));
            }
        }

        if (keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
            System.out.println("------previous media button receive-----");
            if (action == KeyEvent.ACTION_DOWN) {
                context.sendBroadcast(new Intent(MediaPlayBackService.ACTION_PRE));
            }
        }
    }
}
