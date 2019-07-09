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

import android.content.Context;
import android.support.annotation.NonNull;

import fr.arnaudguyon.smartgl.math.Vector3D;
import fr.arnaudguyon.smartgl.opengl.LightParallel;
import fr.arnaudguyon.smartgl.opengl.Object3D;
import fr.arnaudguyon.smartgl.opengl.RenderPassObject3D;
import fr.arnaudguyon.smartgl.opengl.RenderPassSprite;
import fr.arnaudguyon.smartgl.opengl.SmartColor;
import fr.arnaudguyon.smartgl.opengl.SmartGLRenderer;
import fr.arnaudguyon.smartgl.opengl.SmartGLView;
import fr.arnaudguyon.smartgl.opengl.SmartGLViewController;
import fr.arnaudguyon.smartgl.opengl.Sprite;
import fr.arnaudguyon.smartgl.opengl.Texture;
import fr.arnaudguyon.smartgl.tools.WavefrontModel;
import fr.arnaudguyon.smartgl.touch.TouchHelperEvent;

import static java.lang.Math.asin;
import static java.lang.Math.atan2;

/**
 * Created by: Andrew Bates
 */

public class GLViewController implements SmartGLViewController {

    public RawData raw;
    public Quaternion q;
    public Quaternion frameOfReference = new Quaternion(1, 0, 0, 0);
    public RingBuffer buffer = new RingBuffer(20);

    private Sprite mSprite;
    private Object3D mObject3D;

    private float mRandomRotationSpeed;
    private Texture mSpriteTexture;
    private Texture mObjectTexture;
    private Texture mSpaceFrigateTexture;
    private Texture mSpaceCruiserTexture;
    private RenderPassObject3D mRenderPassObject3D;
    private RenderPassObject3D mRenderPassObject3DColor;
    private RenderPassSprite mRenderPassSprite;

    private Object3D teddyBear;
    private Object3D mNextObject = null;
    private Object3D mNextObjectColor = null;

    private double heading = 0.0; // X
    private double attitude = 0.0; // Y
    private double bank = 0.0; // Z

    private int count = 0;

    private Context context;

    public GLViewController(Context context) {
        mRandomRotationSpeed = (float) ((Math.random() * 50) + 100);
        this.context = context;
        if (Math.random() > 0.5f) {
            mRandomRotationSpeed *= -1;
        }
    }

    public void setRaw(double accel_x, double accel_y, double accel_z, double gyro_x, double gyro_y, double gyro_z) {
        raw = new RawData(accel_x, accel_y, accel_z, gyro_x, gyro_y, gyro_z);
        if (!buffer.put(raw)) {
            new ShakeDetection(this.context).execute(buffer);
                for (int i = 0; i < 10; i++) {
                buffer.take();
            }
        }
    }

    public void setQuat(double w, double x, double y, double z) {
        q = new Quaternion(w, x, y, z);
//       q = Quaternion.inverse(Quaternion.qMultiplication(q, frameOfReference));
        q = Quaternion.qMultiplication(Quaternion.inverse(frameOfReference), q);
//        q = Quaternion.qMultiplication(Quaternion.qMultiplication(frameOfReference, q), Quaternion.conjugate(frameOfReference));

    }

    public void setNewFrame(double w, double x, double y, double z) {
        frameOfReference = new Quaternion(w, x, y, z);
    }

    @Override
    public void onPrepareView(SmartGLView smartGLView) {

        Context context = smartGLView.getContext();

        // Add RenderPass for Sprites & Object3D
        SmartGLRenderer renderer = smartGLView.getSmartGLRenderer();
        mRenderPassObject3D = new RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_TEXTURE_LIGHTS, true, true);
        mRenderPassObject3DColor = new RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_COLOR_LIGHTS, true, false);
        mRenderPassSprite = new RenderPassSprite();
        // ORDER MATTERS. FIRST SPRITE THAT IS BACKGROUND AND THEN THE 3D OBJECT!!!!!
        renderer.addRenderPass(mRenderPassSprite);
        renderer.addRenderPass(mRenderPassObject3D);
        renderer.addRenderPass(mRenderPassObject3DColor);

        renderer.getFrameDuration();

        renderer.setDoubleSided(false);

        SmartColor lightColor = new SmartColor(1, 1, 1);
        Vector3D lightDirection = new Vector3D(0.2f, -1, -1);
        lightDirection.normalize();
        LightParallel lightParallel = new LightParallel(lightColor, lightDirection);
        renderer.setLightParallel(lightParallel);

        mSpriteTexture = new Texture(context, R.drawable.planet);
//        mObjectTexture = new Texture(context, R.drawable.coloredbg);
//        mSpaceFrigateTexture = new Texture(context, R.drawable.space_frigate_6_color);
//        mSpaceCruiserTexture = new Texture(context, R.drawable.space_cruiser_4_color);


        mSprite = new Sprite(720, 1230);
//        mSprite.setPivot(0.5f, 0.5f);
//        mSprite.setPos(60, 60);
        mSprite.setTexture(mSpriteTexture);
//        mSprite.setDisplayPriority(100);
        mRenderPassSprite.addSprite(mSprite);


        teddyBear = loadTeddyBear(context);

        switchToTeddyBear();
    }

    @Override
    public void onReleaseView(SmartGLView smartGLView) {
        if (mSpriteTexture != null) {
            mSpriteTexture.release();
            mSpriteTexture = null;
        }
        if (mObjectTexture != null) {
            mObjectTexture.release();
            mObjectTexture = null;
        }
        if (mSpaceFrigateTexture != null) {
            mSpaceFrigateTexture.release();
            mSpaceFrigateTexture = null;
        }
        if (mSpaceCruiserTexture != null) {
            mSpaceCruiserTexture.release();
            mSpaceCruiserTexture = null;
        }
    }

    @Override
    public void onResizeView(SmartGLView smartGLView) {
//        onReleaseView(smartGLView);
//        onPrepareView(smartGLView);
    }

    @Override
    public void onTick(SmartGLView smartGLView) {

        count++;

        Object3D next = mNextObject;
        Object3D nextColor = mNextObjectColor;
        if (next != null) {
            dropAllObject3D();
            mRenderPassObject3D.addObject(mNextObject);
            mObject3D = mNextObject;
            mNextObject = null;
        } else if (nextColor != null) {
            dropAllObject3D();
            mRenderPassObject3DColor.addObject(mNextObjectColor);
            mObject3D = mNextObjectColor;
            mNextObjectColor = null;
        }

        SmartGLRenderer renderer = smartGLView.getSmartGLRenderer();
        float frameDuration = renderer.getFrameDuration();

        if (mObject3D != null) {

            if (q == null) {
                q = frameOfReference;
            }
//
//            if (!q.equals(oldQ)) {
//                oldQ = q;
//                q = Quaternion.inverse(Quaternion.qMultiplication(frameOfReference, q));
//
//            }
//            q = Quaternion.qMultiplication(Quaternion.qMultiplication(frameOfReference, q), Quaternion.conjugate(frameOfReference));
//            q = Quaternion.inverse(Quaternion.qMultiplication(q, frameOfReference));

            double angles[] = Quaternion.quaternionToEulerAngles(q);

            float rx = (float) (angles[0] * 180.0 / Math.PI);
            float ry = (float) (angles[1] * 180.0 / Math.PI);
            float rz = (float) (angles[2] * 180.0 / Math.PI);

            mObject3D.setRotation(rx, ry, rz);
            //rotation = String.format("pitch : %.2f, yaw: %.2f, roll: %.2f", rx, ry, rx);
            //Log.i("rot", rotation);
        }

//        mSpriteTexture = new Texture(context, R.drawable.planet);
//        mSprite = new Sprite(600, 400);
//        mSprite.setPivot(0.5f, 0.5f);
//        mSprite.setPos(60, 60);
//        mSprite.setTexture(mSpriteTexture);
//        mSprite.setDisplayPriority(-1);
//        mRenderPassSprite.addSprite(mSprite);

    }

    @Override
    public void onTouchEvent(SmartGLView smartGLView, TouchHelperEvent event) {
    }

    private void dropAllObject3D() {
//        mRenderPassObject3D.clearObjects();
//        mRenderPassObject3DColor.clearObjects();
    }

    private Object3D loadTeddyBear(@NonNull Context context) {
        WavefrontModel modelColored = new WavefrontModel.Builder(context, R.raw.teddybear2_simplified_obj)//.create();
                .setColor(0.5f, 0.1f, 0.3f)
                .create();
        Object3D object3D = modelColored.toObject3D();
        //object3D.setScale(0.5f, 0.5f, 0.5f);
        object3D.setPos(0, 0, -4);
        return object3D;
    }

    void switchToTeddyBear() {
        mNextObject = null;
        mNextObjectColor = teddyBear;
    }

    // Deprecated, use the Quaterion class function
    // Quaternions to euler angles
    public void qtoa(double w, double x, double y, double z) {
        double sqw = Math.pow(w, 2);
        double sqx = Math.pow(x, 2);
        double sqy = Math.pow(y, 2);
        double sqz = Math.pow(z, 2);
        double unit = sqx + sqy + sqz + sqw;
        double test = x * y + z * w;
        if (test > 0.499 * unit) {
            heading = 2 * atan2(x, w);
            attitude = Math.PI / 2;
            bank = 0;
        } else if (test < -0.499 * unit) {
            heading = -2 * atan2(x, w);
            attitude = -Math.PI / 2;
            bank = 0;
        } else {
            heading = atan2(2 * y * w - 2 * x * z, sqx - sqy - sqz + sqw);
            attitude = asin(2 * test / unit);
            bank = atan2(2 * x * w - 2 * y * z, -sqx + sqy - sqz + sqw);
        }
    }

}
