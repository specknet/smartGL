/*
    Copyright 2016 Arnaud Guyon

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package fr.arnaudguyon.orientteddybear;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

import fr.arnaudguyon.smartgl.opengl.SmartGLView;
import io.reactivex.disposables.Disposable;

public class MainActivity extends Activity {

    private static final String ORIENT_BLE_ADDRESS = "F2:6D:63:1F:17:33"; // test device
    private static final String ORIENT_QUAT_CHARACTERISTIC = "00001526-1212-efde-1523-785feabcd125";
    boolean connected = false;
    private RxBleDevice orient_device;
    private Disposable scanSubscription;
    private RxBleClient rxBleClient;
    private ByteBuffer packetData;
    private Context ctx;
    private SmartGLView mActivityGLView;
    private GLViewController glv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mActivityGLView = findViewById(R.id.activityGLView);
        mActivityGLView.setDefaultRenderer(this);
        glv = new GLViewController();
        mActivityGLView.setController(glv);
        ctx = this;

        packetData = ByteBuffer.allocate(180);
        packetData.order(ByteOrder.LITTLE_ENDIAN);

        rxBleClient = RxBleClient.create(this);

        Log.i("OrientAndroid", "calling scan");
        scan();
    }

    private void scan() {
        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
                // add filters if needed
        )
                .subscribe(
                        scanResult -> {
                            Log.i("OrientAndroid", "FOUND: " + scanResult.getBleDevice().getName() + ", " +
                                    scanResult.getBleDevice().getMacAddress());
                            // Process scan result here.
                            if (scanResult.getBleDevice().getMacAddress().equals(ORIENT_BLE_ADDRESS)) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ctx, "Found " + scanResult.getBleDevice().getName() + ", " +
                                                    scanResult.getBleDevice().getMacAddress(),
                                            Toast.LENGTH_SHORT).show();
                                });
                                connectToOrient(ORIENT_BLE_ADDRESS);
                                scanSubscription.dispose();
                            }
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.i("OrientAndroid", "scan error");
                            runOnUiThread(() -> {
                                Toast.makeText(ctx, "BLE scanning error",
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                );
    }

    private void connectToOrient(String addr) {
        orient_device = rxBleClient.getBleDevice(addr);
        String characteristic;
        characteristic = ORIENT_QUAT_CHARACTERISTIC;

        orient_device.establishConnection(false)
                .flatMap(rxBleConnection -> rxBleConnection.setupNotification(UUID.fromString(characteristic)))
                .doOnNext(notificationObservable -> {
                    // Notification has been set up
                })
                .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                .subscribe(
                        bytes -> {
                            //n += 1;
                            // Given characteristic has been changes, here is the value.

                            //Log.i("OrientAndroid", "Received " + bytes.length + " bytes");
                            if (!connected) {
                                connected = true;

                                runOnUiThread(() -> {
                                    Log.i("OrientAndroid", "receiving sensor data");
                                    Toast.makeText(ctx, "Receiving sensor data",
                                            Toast.LENGTH_SHORT).show();
                                });
                            }
                            handleQuatPacket(bytes);
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.e("OrientAndroid", "Error: " + throwable.toString());
                        }
                );
    }

    private void handleQuatPacket(final byte[] bytes) {
        float divisor_quat = (1 << 30);
        float w = floatFromDataLittle(Arrays.copyOfRange(bytes, 0, 4)) / divisor_quat;
        float x = floatFromDataLittle(Arrays.copyOfRange(bytes, 4, 8)) / divisor_quat;
        float y = floatFromDataLittle(Arrays.copyOfRange(bytes, 8, 12)) / divisor_quat;
        float z = floatFromDataLittle(Arrays.copyOfRange(bytes, 12, 16)) / divisor_quat;

        //q_w = w;
        //q_x = x;
        //q_y = -y;
        //q_z = -z;

        glv.setQuat(w, x, -y, -z);

        //Negating y and z seems to work

        //String q_str = "Quat: (" + w + ", " + x + ", " + y + ", " + z + ")";
        //Log.d("quat", q_str);
    }

    private float floatFromDataLittle(byte[] bytes_slice) {
        // Bytes to float (little endian)
        return java.nio.ByteBuffer.wrap(bytes_slice).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public void calibrateDevice(View view) {
        // Change the frame of reference to calibrate the device
    }

    @Override
    protected void onPause() {
        if (mActivityGLView != null) {
            mActivityGLView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mActivityGLView != null) {
            mActivityGLView.onResume();
        }
    }
}
