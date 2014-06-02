package no.plasmid.fuggel;

import java.io.FileNotFoundException;
import java.nio.FloatBuffer;
import java.util.List;

import no.plasmid.fuggel.im.Renderable;
import no.plasmid.fuggel.im.Vertex;
import no.plasmid.lib.AbstractRenderer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.NVMultisampleFilterHint;

public class Renderer extends AbstractRenderer {

	private int shaderProgramId;

	private int positionAttributeId;
	
	private int projectionMatrixUniformId;
	private int viewMatrixUniformId;
	
	/**
	 * Initialize the renderer, including all shaders
	 */
	public void initializeRenderer() {
		/*
		 * GL context
		 */
		GL11.glViewport(0, 0, Configuration.WINDOW_WIDTH, Configuration.WINDOW_HEIGTH);
		GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		GL11.glCullFace(GL11.GL_BACK);
		GL11.glEnable(GL11.GL_CULL_FACE);

		//Enable multisample anti aliasing
		GL11.glEnable(GL13.GL_MULTISAMPLE);
		GL11.glHint(NVMultisampleFilterHint.GL_MULTISAMPLE_FILTER_HINT_NV, GL11.GL_NICEST);
		
		//Enable texturing
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		checkGL();
		
		/*
		 * Shaders
		 */
		//Load the source
		String vertexSource;
		String fragmentSource;
		try {
			vertexSource = loadTextFile("/shaders/vertex.shader");
			fragmentSource = loadTextFile("/shaders/fragment.shader");
		} catch (FileNotFoundException e) {
			throw new IllegalStateException("Error when opening shader file", e);
		}
		
		//Create the program
		shaderProgramId = GL20.glCreateProgram();
		if (0 == shaderProgramId) {
			throw new IllegalStateException("Could not create shader");
		}
		//Compile the sources
		compileShader(shaderProgramId, vertexSource, GL20.GL_VERTEX_SHADER);
		compileShader(shaderProgramId, fragmentSource, GL20.GL_FRAGMENT_SHADER);
		
		checkGL();

		//Link the program
		GL20.glLinkProgram(shaderProgramId);
		int linkStatus = GL20.glGetProgrami(shaderProgramId, GL20.GL_LINK_STATUS);
		if (0 == linkStatus) {
			String error = GL20.glGetShaderInfoLog(shaderProgramId, 2048);
			throw new IllegalStateException("Error when linking shader: " + error);
		}
		
		checkGL();

		//Validate the program
		GL20.glValidateProgram(shaderProgramId);
    int validationStatus = GL20.glGetProgrami(shaderProgramId, GL20.GL_VALIDATE_STATUS);
    if (0 == validationStatus) {
        String validationError = GL20.glGetProgramInfoLog(shaderProgramId, 2048);
        throw new IllegalStateException("Error found when validating shader: " + validationError);
    }
		checkGL();

		//Find the ID of the position vertex attribute
		positionAttributeId = GL20.glGetAttribLocation(shaderProgramId, "position");
		if (-1 == positionAttributeId) {
			throw new IllegalStateException("Could not find ID of position vertex attribute");
		}
		
		//Find the ID for the projection matrix uniform
		projectionMatrixUniformId = GL20.glGetUniformLocation(shaderProgramId, "projectionMatrix");
		if (-1 == positionAttributeId) {
			throw new IllegalStateException("Could not find ID of projection matrix uniform");
		}
		//Find the ID for the view matrix uniform
		viewMatrixUniformId = GL20.glGetUniformLocation(shaderProgramId, "viewMatrix");
		if (-1 == positionAttributeId) {
			throw new IllegalStateException("Could not find ID of view matrix uniform");
		}
	}
	
	/**
	 * Render the scene
	 */
	public void render(FloatBuffer projectionMatrixBuffer, FloatBuffer viewMatrixBuffer, List<Renderable> renderables) {
		//Clear the display
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

		//Use the shader
    GL20.glUseProgram(shaderProgramId);

    //Enable vertex attributes "position" 
    GL20.glEnableVertexAttribArray(positionAttributeId);
    
    //Set projection matrix
    GL20.glUniformMatrix4(projectionMatrixUniformId, false, projectionMatrixBuffer);
    //Set view matrix
    GL20.glUniformMatrix4(viewMatrixUniformId, false, viewMatrixBuffer);
    
    for (Renderable renderable : renderables) {
      //Bind data buffer
      GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, renderable.getBufferId());
  		GL20.glVertexAttribPointer(positionAttributeId, 3, GL11.GL_FLOAT, false, 3 * 4, 0);	//Each vertex is 5 floats * 4 bytes
  		
  		//Draw
  		GL11.glDrawArrays(GL11.GL_TRIANGLE_STRIP, 0, renderable.getVertices().size());
    }
    
		//Disable vertex attribute arrays
		GL20.glDisableVertexAttribArray(positionAttributeId);
	}
	
	public void registerRenderable(Renderable renderable) {
		List<Vertex> vertices = renderable.getVertices();
		renderable.setBufferId(GL15.glGenBuffers());
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, renderable.getBufferId());
		//Reserve enough space for position.
		FloatBuffer data = BufferUtils.createFloatBuffer((vertices.size() * 3));
		for (Vertex vertex : vertices) {
			vertex.store(data);
		}
		data.rewind();
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, data, GL15.GL_STATIC_DRAW);

		checkGL();
	}
		
}
