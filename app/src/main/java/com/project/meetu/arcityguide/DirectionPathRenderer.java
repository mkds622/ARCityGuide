package com.project.meetu.arcityguide;

/**
 * Created by Meetu on 11-04-2017.
 */

import static android.opengl.GLES20.GL_BLEND;
import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_ONE_MINUS_SRC_ALPHA;
import static android.opengl.GLES20.GL_SRC_ALPHA;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.glBlendFunc;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnable;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;


import com.project.meetu.arcityguide.util.LoggerConfig;
import com.project.meetu.arcityguide.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class DirectionPathRenderer implements GLSurfaceView.Renderer {
    private static final int POSITION_COMPONENT_COUNT = 4;
    private static final String U_COLOR = "u_Color";
    private static int uColorLocation;
    private static int uColorLocation1;
    private static final String A_POSITION = "a_Position";
    private int aPositionLocation;
    private static final int BYTES_PER_FLOAT = 4;
    private FloatBuffer vertexData;
    private final Context context;
    private int program;
    float[] arrowVerticesWithTriangles;
    float degreeToBePointed;
    float s;
    int start,end;
    float[] arrowVerticesWithTriangles1;

    public void setDegreeAndCalculatePoints(float degree){
        degreeToBePointed=degree;
        s=(float)(1.6*Math.tan(Math.toRadians(degreeToBePointed))/30);
        float a1x = 0.0f, a1y= -0.50f , b1x = -0.8f, b1y= -0.65f, c1x = -0.80f, c1y= -0.80f, d1x = 0.0f, d1y= -0.65f, e1x = 0.80f, e1y= -0.80f, f1x = 0.80f, f1y= -0.65f;
//        float a5x = 0.360f, a5y= 0.25f , b5x = 0.21f, b5y= 0.25f, c5x = 0.110f, c5y= 0.150f, d5x = 0.260f, d5y= 0.150f, e5x = 0.410f, e5y= 0.050f, f5x = 0.510f, f5y= 0.150f;
        float z=0;
        arrowVerticesWithTriangles=new float[]
                {
                        //Base arrow
                        b1x,b1y,z,2+b1y,c1x,c1y,z,2+c1y,d1x,d1y,z,2+d1y,
                        b1x,b1y,z,2+b1y,d1x,d1y,z,2+d1y,f1x,f1y,z,2+f1y,
                        d1x,d1y,z,2+d1y,e1x,e1y,z,2+e1y,f1x,f1y,z,2+f1y,
                        b1x,b1y,z,2+b1y,f1x,f1y,z,2+f1y,a1x,a1y,z,2+a1y,

//2nd arrow
                        b1x+(3*s),b1y+(1*s)+0.25f,z,2+b1y+(1*s*s)+0.25f,c1x+(1*s),c1y+(1*s)+0.25f,z,2+c1y+(1*s*s)+0.25f,d1x+(1*s),d1y+(0*s)+0.25f,z,2+d1y+(0*s*s)+0.25f,
                        b1x+(3*s),b1y+(1*s)+0.25f,z,2+b1y+(1*s*s)+0.25f,d1x+(1*s),d1y+(0*s)+0.25f,z,2+d1y+(0*s*s)+0.25f,f1x+(3*s),f1y+(-1*s)+0.25f,z,2+f1y+(-1*s*s)+0.25f,
                        d1x+(1*s),d1y+(0*s)+0.25f,z,2+d1y+(0*s*s)+0.25f,e1x+(1*s),e1y+(-1*s)+0.25f,z,2+e1y+(-1*s*s)+0.25f,f1x+(3*s),f1y+(-1*s)+0.25f,z,2+f1y+(-1*s*s)+0.25f,
                        b1x+(3*s),b1y+(1*s)+0.25f,z,2+b1y+(1*s*s)+0.25f,f1x+(3*s),f1y+(-1*s)+0.25f,z,2+f1y+(-1*s*s)+0.25f,a1x+(3*s),a1y+(0*s)+0.25f,z,2+a1y+(0*s*s)+0.25f,

//3rd arrow
                        b1x+(9*s),b1y+(2*s)+0.5f,z,2+b1y+(2*s*s)+0.5f,c1x+(5*s),c1y+(2*s)+0.5f,z,2+c1y+(2*s*s)+0.5f,d1x+(5*s),d1y+(0*s)+0.5f,z,2+d1y+(0*s*s)+0.5f,
                        b1x+(9*s),b1y+(2*s)+0.5f,z,2+b1y+(2*s*s)+0.5f,d1x+(5*s),d1y+(0*s)+0.5f,z,2+d1y+(0*s*s)+0.5f,f1x+(9*s),f1y+(-2*s)+0.5f,z,2+f1y+(-2*s*s)+0.5f,
                        d1x+(5*s),d1y+(0*s)+0.5f,z,2+d1y+(0*s*s)+0.5f,e1x+(5*s),e1y+(-2*s)+0.5f,z,2+e1y+(-2*s*s)+0.5f,f1x+(9*s),f1y+(-2*s)+0.5f,z,2+f1y+(-2*s*s)+0.5f,
                        b1x+(9*s),b1y+(2*s)+0.5f,z,2+b1y+(2*s*s)+0.5f,f1x+(9*s),f1y+(-2*s)+0.5f,z,2+f1y+(-2*s*s)+0.5f,a1x+(9*s),a1y+(0*s)+0.5f,z,2+a1y+(0*s*s)+0.5f,

//4th arrow
                        b1x+(18*s),b1y+(3*s)+0.75f,z,2+b1y+(3*s*s)+0.75f,c1x+(12*s),c1y+(3*s)+0.75f,z,2+c1y+(3*s*s)+0.75f,d1x+(12*s),d1y+(0*s)+0.75f,z,2+d1y+(0*s*s)+0.75f,
                        b1x+(18*s),b1y+(3*s)+0.75f,z,2+b1y+(3*s*s)+0.75f,d1x+(12*s),d1y+(0*s)+0.75f,z,2+d1y+(0*s*s)+0.75f,f1x+(18*s),f1y+(-3*s)+0.75f,z,2+f1y+(-3*s*s)+0.75f,
                        d1x+(12*s),d1y+(0*s)+0.75f,z,2+d1y+(0*s*s)+0.75f,e1x+(12*s),e1y+(-3*s)+0.75f,z,2+e1y+(-3*s*s)+0.75f,f1x+(18*s),f1y+(-3*s)+0.75f,z,2+f1y+(-3*s*s)+0.75f,
                        b1x+(18*s),b1y+(3*s)+0.75f,z,2+b1y+(3*s*s)+0.75f,f1x+(18*s),f1y+(-3*s)+0.75f,z,2+f1y+(-3*s*s)+0.75f,a1x+(18*s),a1y+(0*s)+0.75f,z,2+a1y+(0*s*s)+0.75f,

//5th arrow
                        b1x+(30*s),b1y+(4*s)+1f,z,2+b1y+(4*s*s)+1f,c1x+(22*s),c1y+(4*s)+1f,z,2+c1y+(4*s*s)+1f,d1x+(22*s),d1y+(0*s)+1f,z,2+d1y+(0*s*s)+1f,
                        b1x+(30*s),b1y+(4*s)+1f,z,2+b1y+(4*s*s)+1f,d1x+(22*s),d1y+(0*s)+1f,z,2+d1y+(0*s*s)+1f,f1x+(30*s),f1y+(-4*s)+1f,z,2+f1y+(-4*s*s)+1f,
                        d1x+(22*s),d1y+(0*s)+1f,z,2+d1y+(0*s*s)+1f,e1x+(22*s),e1y+(-4*s)+1f,z,2+e1y+(-4*s*s)+1f,f1x+(30*s),f1y+(-4*s)+1f,z,2+f1y+(-4*s*s)+1f,
                        b1x+(30*s),b1y+(4*s)+1f,z,2+b1y+(4*s*s)+1f,f1x+(30*s),f1y+(-4*s)+1f,z,2+f1y+(-4*s*s)+1f,a1x+(30*s),a1y+(0*s)+1f,z,2+a1y+(0*s*s)+1f,


//                //triangle 1
//                -0.15f,-0.05f,
//                a1x,a1x,
//                -0.15f,0.05f,
//
//                //trianlge 2
//                -0.15f,0.05f,
//                0f,0f,
//                0.15f,0.05f,
//
//                //triangle 3
//                0f,0f,
//                0.15f,-0.05f,
//                0.15f,0.05f,
//
//                //triangle 4
//                -0.15f,0.05f,
//                0.15f,0.05f,
//                0f,0.1f,

                };
        if(vertexData!=null) {
            vertexData.clear();
            vertexData.put(arrowVerticesWithTriangles);
        }
        }
    public DirectionPathRenderer(Context context){
        this.context = context;
        setDegreeAndCalculatePoints(0f);

//        vertexData = ByteBuffer
//                .allocateDirect(arrowVerticesWithTriangles.length * BYTES_PER_FLOAT)
//                .order(ByteOrder.nativeOrder())
//                .asFloatBuffer();
//        vertexData.put(arrowVerticesWithTriangles);
        vertexData = ByteBuffer
                .allocateDirect(arrowVerticesWithTriangles.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

        Log.e("DirectionRenderer:","vertexData="+vertexData);
        vertexData.put(arrowVerticesWithTriangles);
        start=0;
        end=60;

       }
    @Override
    public void onSurfaceCreated(GL10 glUnused, javax.microedition.khronos.egl.EGLConfig config) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.0f,0.0f, 0.0f, 0.05f);
        String vertexShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.simple_vertex_shader);
        String fragmentShaderSource = TextResourceReader
                .readTextFileFromResource(context, R.raw.simple_fragment_shader);
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);
        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }
        glUseProgram(program);
        uColorLocation = glGetUniformLocation(program, U_COLOR);
        aPositionLocation = glGetAttribLocation(program, A_POSITION);
        vertexData.position(0);

        glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GL_FLOAT,
                false, 0, vertexData);
        glEnableVertexAttribArray(aPositionLocation);

    }
    @Override
    public void onSurfaceChanged(GL10 glUnused, int width, int height) {
// Set the OpenGL viewport to fill the entire surface.
        glViewport(0, 0, width, height);
    }
    @Override
    public void onDrawFrame(GL10 glUnused) {
// Clear the rendering surface.
        glClear(GL_COLOR_BUFFER_BIT);
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 0.8f);
        glDrawArrays(GL_TRIANGLES, 0, 60);
    }

    public void DrawFrame(float degree){
        setDegreeAndCalculatePoints(degree);
//        start+=60;
//        end+=60;
        glClear(GL_COLOR_BUFFER_BIT);

        //glEnableVertexAttribArray(aPositionLocation);
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 0.5f);
        glDrawArrays(GL_TRIANGLES, start, end);

    }
    public void CalculateRightPoints(){


        arrowVerticesWithTriangles1=new float[]
                {
                        //Base arrow
                        0f,0f,0f,1f,0.3f,0.4f,0f,1f,0,0.4f,0f,1f,
                        0f,0f,0f,1f,0,0.4f,0f,1f,-0.3f,0,0f,1f,
                        0f,0f,0f,1f,-0.3f,0,0f,1f,0,-0.4f,0f,1f,
                        0f,0f,0f,1f,0,-0.4f,0f,1f,0.3f,-0.4f,0f,1f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f
                };
        if(vertexData!=null) {
            vertexData.clear();
            vertexData.put(arrowVerticesWithTriangles1);
        }

    }

    public void drawFrame1(){
        CalculateRightPoints();
//        start+=60;
//        end+=60;

        glClear(GL_COLOR_BUFFER_BIT);
        glEnableVertexAttribArray(aPositionLocation);
        glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 0.5f);
        glDrawArrays(GL_TRIANGLES, 0, 12);

    }
    public void CalculateLeftPoints(){

        arrowVerticesWithTriangles1=new float[]
                {
                        //Base arrow
                        0f,0f,0f,1f,0f,0.4f,0f,1f,-0.3f,0.4f,0f,1f,
                        0f,0f,0f,1f,0.3f,0f,0f,1f,0f,0.4f,0f,1f,
                        0f,0f,0f,1f,0,-0.4f,0f,1f,0.3f,0f,0f,1f,
                        0f,0f,0f,1f,-0.3f,-0.4f,0f,1f,0,-0.4f,0f,1f,


                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f
                };
        if(vertexData!=null) {
            vertexData.clear();
            vertexData.put(arrowVerticesWithTriangles1);

        }

    }
    public void drawFrame2(){
        CalculateLeftPoints();
//        start+=60;
//        end+=60;
        glClear(GL_COLOR_BUFFER_BIT);

        glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 0.5f);
        glDrawArrays(GL_TRIANGLES, 0, 12);


    }
    public void CalculateDownPoints(){


        arrowVerticesWithTriangles1=new float[]
                {
                        //Base arrow
                        -0.75f,0,0f,1f,-0.75f,-0.3f,0f,1f,0,-0.3f,0f,1f,
                        0,-0.3f,0f,1f,-0.75f,-0.3f,0f,1f,0,-0.6f,0f,1f,
                        0,-0.3f,0f,1f,0,-0.6f,0f,1f,0.75f,-0.3f,0f,1f,
                        0,-0.3f,0f,1f,0.75f,-0.3f,0f,1f,0.75f,0,0f,1f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,

                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                        0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,0f,
                };
        if(vertexData!=null) {
            vertexData.clear();
            vertexData.put(arrowVerticesWithTriangles1);
        }


    }

    public void drawFrame3(){
        CalculateDownPoints();
//        start+=60;
//        end+=60;

        glClear(GL_COLOR_BUFFER_BIT);

        glUniform4f(uColorLocation, 0.0f, 0.0f, 0.0f, 1.0f);
        glDrawArrays(GL_TRIANGLES, 0, 12);

    }
}