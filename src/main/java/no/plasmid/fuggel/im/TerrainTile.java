package no.plasmid.fuggel.im;

import java.util.List;

import no.plasmid.fuggel.Configuration;

import org.lwjgl.util.vector.Vector3f;

public class TerrainTile extends Renderable {

	public TerrainTile(int worldX, int worldZ, float x1z1, float x2z1, float x1z2, float x2z2) {
		List<Vertex> vertices = this.getVertices();
		vertices.add(new Vertex(new Vector3f((worldX + 1) * Configuration.TERRAIN_TILE_SIZE, x2z1, worldZ * Configuration.TERRAIN_TILE_SIZE)));
		vertices.add(new Vertex(new Vector3f(worldX * Configuration.TERRAIN_TILE_SIZE, x1z1, worldZ * Configuration.TERRAIN_TILE_SIZE)));
		vertices.add(new Vertex(new Vector3f((worldX + 1) * Configuration.TERRAIN_TILE_SIZE, x2z2, (worldZ + 1) * Configuration.TERRAIN_TILE_SIZE)));
		vertices.add(new Vertex(new Vector3f(worldX * Configuration.TERRAIN_TILE_SIZE, x1z2, (worldZ + 1) * Configuration.TERRAIN_TILE_SIZE)));
	}
	
	public void createMesh() {
		
	}
	
}
