/*
    Copyright 2017 Arnaud Guyon

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
package fr.arnaudguyon.smartgl.opengl;

import android.opengl.GLES20;

import java.nio.FloatBuffer;

import fr.arnaudguyon.smartgl.tools.Assert;

/**
 * Created by aguyon on 18.01.17.
 */

public class ShaderTextureLights extends Shader {

    private final static String VERTEX_SHADER =
            "uniform mat4 m_ProjectionMatrix;" +
                    "uniform mat4 mModelMatrix;" +
                    "uniform vec3 mParallelLightDirection;" +
                    "uniform vec4 mParallelLightColor;" +
                    "uniform vec4 mAmbiantColor;" +
                    "attribute vec4 m_Position;" +
                    "attribute vec2 m_UV;" +
                    "attribute vec3 mNormals;" +
                    "varying vec2 vTextureCoord;" +
                    "varying vec4 vDirectColor;" +
                    "varying vec4 vIndirectColor;" +
                    "void main() {" +
                    "  gl_Position = m_ProjectionMatrix * m_Position;" +
                    "  vTextureCoord = m_UV;" +
                    "  vec3 modelViewNormal = vec3(mModelMatrix * vec4(mNormals, 0.0));" +
                    "  float intensity = max(dot(modelViewNormal, -mParallelLightDirection), 0.0);" +
                    "  vDirectColor = mAmbiantColor + (mParallelLightColor * intensity);" +
                    "  vIndirectColor = mParallelLightColor * intensity * 0.25;" +
                    "}";

    private final static String PIXEL_SHADER =
            "precision mediump float;" +
                    "varying vec2 vTextureCoord;" +
                    "varying vec4 vDirectColor;" +
                    "varying vec4 vIndirectColor;" +
                    "uniform sampler2D sTexture;" +
                    "void main() {" +
                    "  gl_FragColor = (texture2D(sTexture, vTextureCoord) * vDirectColor) + vIndirectColor;" +
                    "}";

    private int mModelMatrixId;
    private int mParallelLightDirectionId;
    private int mParallelLightColorId;
    private int mLightAmbiantId;
    private int mNormalsId;

    public ShaderTextureLights() {
        super(VERTEX_SHADER, PIXEL_SHADER);
    }

    @Override
    public boolean useTexture() {
        return true;
    }

    @Override
    public boolean useColor() {
        return false;
    }

    @Override
    protected String getVertexAttribName() {
        return "m_Position";
    }

    @Override
    protected String getUVAttribName() {
        return "m_UV";
    }

    @Override
    protected String getColorAttribName() {
        return null;
    }

    @Override
    protected String getProjMatrixAttribName() {
        return "m_ProjectionMatrix";
    }

    @Override
    protected void init(int programId) {
        super.init(programId);

        mModelMatrixId = GLES20.glGetUniformLocation(programId, "mModelMatrix");
        mParallelLightDirectionId = GLES20.glGetUniformLocation(programId, "mParallelLightDirection");
        mParallelLightColorId = GLES20.glGetUniformLocation(programId, "mParallelLightColor");
        mLightAmbiantId = GLES20.glGetUniformLocation(programId, "mAmbiantColor");
        mNormalsId = GLES20.glGetAttribLocation(programId, "mNormals");
        Assert.assertTrue(mModelMatrixId >= 0);
        Assert.assertTrue(mParallelLightDirectionId >= 0);
        Assert.assertTrue(mParallelLightColorId >= 0);
        Assert.assertTrue(mLightAmbiantId >= 0);
        Assert.assertTrue(mNormalsId >= 0);
    }

    @Override
    public void onPreRender(OpenGLRenderer renderer, RenderObject object, Face3D face3D) {

        float[] modelMatrix = object.getMatrix();
        GLES20.glUniformMatrix4fv(mModelMatrixId, 1, false, modelMatrix, 0);

        float[] lightDirection = renderer.getLightDirection();
        GLES20.glUniform3fv(mParallelLightDirectionId, 1, lightDirection, 0);

        float[] lightColor = renderer.getLightColor();
        GLES20.glUniform4fv(mParallelLightColorId, 1, lightColor, 0);

        float[] ambiant = renderer.getLightAmbiant();
        GLES20.glUniform4fv(mLightAmbiantId, 1, ambiant, 0);

        GLES20.glEnableVertexAttribArray(mNormalsId);
        NormalList normalList = face3D.getNormalList();
        FloatBuffer vertexBuffer = normalList.getFloatBuffer();
        vertexBuffer.position(0);
        GLES20.glVertexAttribPointer(mNormalsId, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
    }

}
