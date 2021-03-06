package com.fwumdesoft.project8;

import java.awt.Point;
import java.text.DecimalFormat;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Align;
import com.fwumdesoft.project8.CircuitComponent.Type;
import com.fwumdesoft.project8.Overworld.Modifier;
import com.fwumdesoft.project8.Overworld.Tile;

/**
 * Draws the game to separate drawing from simulation
 */
public class Renderer
{
	private SpriteBatch batch;
	private ShapeRenderer shapes;
	private BitmapFont font;
	/**
	 * The camera offset for the circuits
	 */
	private Vector2 circuitOffset;
	/**
	 * The number of pixels of the side of an overworld square
	 */
	public int cellSize;
	/**
	 * The number of pixels of the side of a circuit.grid component
	 */
	private int componentSize;
	/**
	 * The number of pixels of the screen's width
	 */
	private int screenWidth;
	/**
	 * The number of pixels of the screen's height
	 */
	private int screenHeight;
	private Texture wall, floor, pod, componentPile, componentMachine, fireSuppression, terminal,
						resistor, lamp, battery, cursor, blank, filledBlank, credits;
	/**
	 * All of the individual wire tileset images [right][top][left][bottom]
	 */
	private TextureRegion[][][][] wireTiles;
	private TextureRegion unconnectedWire;
	/**
	 * The frames in the fire animation
	 */
	private TextureRegion[] fire, playerWalk, door;
	/**
	 * The current frame in the fire animation
	 */
	private int fireFrame;
	/**
	 * If the class should draw the inventory </br>
	 * Toggled by tab
	 */
	private boolean showInventory;
	/**
	 * The location of the circuit.grid camera
	 */
	private Vector2 circuitCamera;
	/**
	 * The current animation frame for the overworld
	 */
	private int currentFrame;
	/**
	 * The game frames per one animation
	 */
	//private final int FRAMES_PER_ANIMATION = 15; 
	/**
	 * A formatter to print numbers to the player correctly
	 */
	private DecimalFormat sigFigs;
	/**
	 * Create a Renderer
	 * 
	 * @param batch
	 *            A SpriteBatch which should be disposed of when the Renderer is
	 *            unnecessary
	 * @param font
	 *            The font used to render the UI
	 * @param assets
	 *            An AssetManager with the game assets loaded
	 * @param cellSize
	 *            The size of an overworld square
	 * @param componentSize
	 *            The size of a circuit.grid component
	 * @param screenWidth
	 *            The width of the screen
	 * @param screenHeight
	 *            The height of the screen
	 */
	public Renderer(SpriteBatch batch, BitmapFont font, AssetManager assets, int cellSize, int componentSize,
			int screenWidth, int screenHeight, Vector2 camera)
	{
		// Initialize member variables
		this.batch = batch;
		this.font = font;
		this.shapes = new ShapeRenderer();
		this.cellSize = cellSize;
		this.componentSize = componentSize;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		// Retrieve image assets
		this.wall = assets.get("station_wall.png", Texture.class);
		this.floor = assets.get("station_floor.png", Texture.class);
		this.pod = assets.get("escape_pod.png", Texture.class);
		Texture fire = assets.get("fire.png", Texture.class);
		this.fire = new TextureRegion[4];
		for(int i = 0; i < 4; i++)
			this.fire[i] = new TextureRegion(fire, fire.getWidth() / 4 * i, 0, fire.getWidth() / 4, fire.getHeight());
		Texture playerWalk = assets.get("player_walking.png", Texture.class);
		this.playerWalk = new TextureRegion[3];
		for(int i = 0; i < this.playerWalk.length; i++)
			this.playerWalk[i] = new TextureRegion(playerWalk, playerWalk.getWidth() / this.playerWalk.length * i, 0,
					playerWalk.getWidth() / this.playerWalk.length, playerWalk.getHeight());
		Texture door = assets.get("station_door.png", Texture.class);
		this.door = new TextureRegion[5];
		for(int i = 0; i < this.door.length; i++)
			this.door[i] = new TextureRegion(door, door.getWidth() / this.door.length * i, 0,
					door.getWidth() / this.door.length, door.getHeight());
		this.componentPile = assets.get("component_pile.png", Texture.class);
		this.componentMachine = assets.get("component_machine.png", Texture.class);
		this.fireSuppression = assets.get("fire_suppression.png", Texture.class);
		this.terminal = assets.get("terminal.png", Texture.class); 
		this.resistor = assets.get("resistor.png", Texture.class);
		this.lamp = assets.get("lamp.png", Texture.class);
		this.battery = assets.get("battery.png", Texture.class);
		this.cursor = assets.get("cursor.png", Texture.class);
		this.blank = assets.get("blank.png", Texture.class);
		this.filledBlank = assets.get("filled_blank.png", Texture.class);
		this.credits = assets.get("credits.png", Texture.class);
		
		this.circuitOffset = new Vector2();
		// Create wire tileset
		Texture wires = assets.get("wires.png", Texture.class);
		wireTiles = new TextureRegion[2][2][2][2];
		int size = 64;
		unconnectedWire = new TextureRegion(wires, 0, size * 2, size, size);
		wireTiles[1][0][1][0] = new TextureRegion(wires, 0, 0, size, size);
		wireTiles[0][1][0][1] = new TextureRegion(wires, 0, size, size, size);
		wireTiles[1][0][0][1] = new TextureRegion(wires, size, 0, size, size);
		wireTiles[1][0][1][1] = new TextureRegion(wires, size * 2, 0, size, size);
		wireTiles[0][0][1][1] = new TextureRegion(wires, size * 3, 0, size, size);
		wireTiles[1][1][0][1] = new TextureRegion(wires, size, size, size, size);
		wireTiles[1][1][1][1] = new TextureRegion(wires, size * 2, size, size, size);
		wireTiles[0][1][1][1] = new TextureRegion(wires, size * 3, size, size, size);
		wireTiles[1][1][0][0] = new TextureRegion(wires, size, size * 2, size, size);
		wireTiles[1][1][1][0] = new TextureRegion(wires, size * 2, size * 2, size, size);
		wireTiles[0][1][1][0] = new TextureRegion(wires, size * 3, size * 2, size, size);
		showInventory = true;
		circuitCamera = camera;
		sigFigs = new DecimalFormat("0.00");
	}

	/**
	 * The distance the player needs to be from a door for it to open
	 */
	private final int doorOpenDistance = 1;

	/**
	 * Draw the current state of the overworld
	 * 
	 * @param world
	 */
	public void renderOverworld(Overworld world, Inventory inventory)
	{
		if(world.playFire)
			App.playSound(App.sounds.fire, 1);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Color.BLACK);
		shapes.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapes.end();
		if(currentFrame > 0)
			currentFrame++;
		if(currentFrame >= OverworldInput.MAX_COOLDOWN)
			currentFrame = 0;
		if(world.playerMoving)
			currentFrame = 1;
		world.playerMoving = false;
		this.fireFrame = (fireFrame + 1) % 60;
		int fireFrame = this.fireFrame / 15;
		Point player = world.playerPos;
		// Establish the drawable region
		int halfGridWidth = (screenWidth / cellSize) / 2;
		int halfGridHeight = (screenHeight / cellSize) / 2;
		int xStart = Math.max(0, player.x - halfGridWidth - 1);
		int xEnd = Math.min(world.map.length, player.x + halfGridWidth + 1);
		int yStart = Math.max(0, player.y - halfGridHeight - 1);
		int yEnd = Math.min(world.map[0].length, player.y + halfGridHeight + 1 + cellSize);
		batch.begin();
		for (int y = yStart; y < yEnd; y++)
		{
			for (int x = xStart; x < xEnd; x++)
			{
				// Find the position where the square will draw
				int drawX = (x - player.x + (currentFrame != 0 ? world.playerFace.x : 0) + halfGridWidth) * cellSize +
						(int)(cellSize * currentFrame / OverworldInput.MAX_COOLDOWN) * -world.playerFace.x;
				int drawY = (y - player.y + (currentFrame != 0 ? world.playerFace.y : 0) + halfGridHeight) * cellSize +
						(int)(cellSize * currentFrame / OverworldInput.MAX_COOLDOWN) * -world.playerFace.y;
				if((world.map[y][x] == Tile.door || world.map[y][x] == Tile.fireSuppression || world.map[y][x] == Tile.terminal)
						&& world.modifiers[y][x] == Modifier.broken && Math.random() < 0.05)
				{
					ParticleSystem.burst("spark", drawX + cellSize / 2, drawY + cellSize / 2, 12);
					App.playSound(App.sounds.sparks, (float)world.playerPos.distance(x,  y));
				}
				// Draw the correct texture
				switch (world.map[y][x])
				{
				case wall:
					batch.draw(wall, drawX, drawY);
					break;
				case door:
					TextureRegion t = door[2];
					if (world.modifiers[y][x] == Modifier.broken)
						t = door[0];
					else if(currentFrame == 0)
						if(Vector2.dst(x, y, world.playerPos.x, world.playerPos.y) <= doorOpenDistance)
							t = door[4];
						else
							t = door[0];
					else if(Vector2.dst(x, y, world.playerPos.x, world.playerPos.y) <= doorOpenDistance &&
							Vector2.dst(x, y, world.previousPlayerPos.x, world.previousPlayerPos.y) <= doorOpenDistance)
						t = door[4];
					else if(Vector2.dst(x, y, world.playerPos.x, world.playerPos.y) > doorOpenDistance &&
							Vector2.dst(x, y, world.previousPlayerPos.x, world.previousPlayerPos.y) > doorOpenDistance)
						t = door[0];
					else if(Vector2.dst(x, y, world.playerPos.x, world.playerPos.y) > doorOpenDistance &&
							Vector2.dst(x, y, world.previousPlayerPos.x, world.previousPlayerPos.y) <= doorOpenDistance)
					{
						t = door[door.length - 1 - (int)((double)currentFrame / OverworldInput.MAX_COOLDOWN * door.length)];
						App.playSound(App.sounds.door, doorOpenDistance);
					}
					else if(Vector2.dst(x, y, world.playerPos.x, world.playerPos.y) <= doorOpenDistance &&
							Vector2.dst(x, y, world.previousPlayerPos.x, world.previousPlayerPos.y) > doorOpenDistance)
					{
						t = door[(int)((double)currentFrame / OverworldInput.MAX_COOLDOWN * door.length)];
						App.playSound(App.sounds.door, doorOpenDistance);
					}
					float rotation = 0;
					if (y > 0 && world.map[y - 1][x] != Tile.wall)
						rotation = 90;
					draw(batch, t, drawX, drawY, cellSize / 2, cellSize / 2, rotation);
					if(world.modifiers[y][x] == Modifier.fire)
						batch.draw(fire[fireFrame], drawX, drawY);
					break;
				case floor:
					batch.draw(floor, drawX, drawY);
					if(world.modifiers[y][x] == Modifier.componentPile)
						batch.draw(componentPile, drawX, drawY);
					else if(world.modifiers[y][x] == Modifier.fire)
						batch.draw(fire[fireFrame], drawX, drawY);
					break;
				case pod:
					rotation = 0;
					if(world.map[y - 1][x] == Overworld.Tile.door)
						rotation = 0;
					else if(world.map[y][x + 1] == Overworld.Tile.door)
						rotation = 90;
					else if(world.map[y + 1][x] == Overworld.Tile.door)
						rotation = 180;
					else if(world.map[y][x - 1] == Overworld.Tile.door)
						rotation = 270;
					draw(batch, pod, drawX, drawY, cellSize / 2, cellSize / 2, rotation);
					break;
				case fireSuppression:
					batch.draw(fireSuppression, drawX, drawY);
					break;
				case componentMachine:
					batch.draw(componentMachine, drawX, drawY);
					break;
				case terminal:
					batch.draw(terminal, drawX, drawY);
					break;
				default:
					break;
				}
			}
		}
		// Draw the player, centered on the screen
		// Because all drawing is centered on the player is guaranteed to be
		// centered
		float rotation = (float) Math.atan2(world.playerFace.y, world.playerFace.x);
		rotation = (float) Math.toDegrees(rotation);
		draw(batch, playerWalk[(int)((double)currentFrame / OverworldInput.MAX_COOLDOWN * playerWalk.length)],
				halfGridWidth * cellSize, halfGridHeight * cellSize, cellSize / 2, cellSize / 2, rotation-90);
		ParticleSystem.draw(batch);
		batch.end();
		renderInventory(inventory);
		//Draw the health indicator
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Color.RED);
		for(int i = 0; i < world.MAX_PLAYER_HEALTH; i++)
			shapes.rect(i * 24 + 4, Gdx.graphics.getHeight() - 20, 16, 16);
		shapes.setColor(Color.BLUE);
		for(int i = 0; i < world.playerHealth; i++)
			shapes.rect(i * 24 + 4, Gdx.graphics.getHeight() - 20, 16, 16);
		Gdx.gl.glEnable(GL30.GL_BLEND);
		shapes.setColor(new Color(1, 0, 0, 0.15f));
		for(int i = 0; i < world.MAX_PLAYER_HEALTH - world.playerHealth; i++)
			shapes.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		shapes.end();
	}

	public void renderCircuit(Circuit circuit, Inventory inventory, int cursorX, int cursorY)
	{
		this.fireFrame = (fireFrame + 1) % 60;
		int fireFrame = this.fireFrame / 15;
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Color.GRAY);
		shapes.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		font.setColor(Color.WHITE);
		shapes.setColor(Color.WHITE);
		shapes.rect(-circuitCamera.x, -circuitCamera.y, circuit.grid[0].length * componentSize, circuit.grid.length * componentSize);
		if (showInventory)
		{
			shapes.setColor(Color.BLACK);
			shapes.rect(464, 0, 640, 96);
		}
		shapes.end();
		batch.begin();
		for (int y = 0; y < circuit.grid.length; y++)
		{
			for (int x = 0; x < circuit.grid[y].length; x++)
			{
				boolean top, left, right, bottom;
				bottom = y > 0 && circuit.grid[y - 1][x] != null;
				left = x > 0 && circuit.grid[y][x - 1] != null;
				right = x < circuit.grid[y].length - 1 && circuit.grid[y][x + 1] != null;
				top = y < circuit.grid.length - 1 && circuit.grid[y + 1][x] != null;
				int drawX = x * componentSize - (int)circuitCamera.x;
				int drawY = y * componentSize - (int)circuitCamera.y;
				CircuitComponent comp = circuit.grid[y][x];
				if (comp == null)
				{
					continue;
				}
				if (comp.type == Type.WIRE)
				{
					TextureRegion region = wireTiles[right ? 1 : 0][top ? 1 : 0][left ? 1 : 0][bottom ? 1 : 0];
					region = region != null ? region : unconnectedWire;
					batch.draw(region, drawX, drawY);
				} else
				{
					if(comp.isChangeable && comp.type == null && Math.random() < 0.005)
						ParticleSystem.burst("electricity", drawX + componentSize / 2, drawY + componentSize / 2, 5);
					int rotation = (top && bottom) ? 90 : 0;
					Texture tex;
					if (comp.isChangeable)
					{
						Texture t = comp.type == null ? blank : filledBlank;
						draw(batch, t, drawX, drawY, componentSize / 2, componentSize / 2, rotation);
					}
					if (comp.type == Type.RESISTOR)
					{
						tex = comp.isLamp ? lamp : resistor;
					} else if (comp.type == Type.BATTERY)
					{
						tex = battery;
					} else
					{
						continue;
					}
					if(comp.isActive)
						batch.setColor(Color.GREEN);
					else
						batch.setColor(Color.RED);
					draw(batch, tex, drawX, drawY, componentSize / 2, componentSize / 2, rotation);
					batch.setColor(Color.WHITE);
					if(Double.isNaN(comp.current))
					{
						batch.draw(fire[fireFrame], drawX, drawY, componentSize, componentSize);
						App.playSound(App.sounds.fire, 1);
					}
				}
			}
		}
		if (showInventory && cursorX >= 0 && cursorY >= 0 && cursorY < circuit.grid.length
				&& cursorX < circuit.grid[cursorY].length)
		{
			CircuitComponent comp = circuit.grid[cursorY][cursorX];
			if (comp != null && comp.type != null)
			{
				String outValue = "";
				switch (comp.type)
				{
				case RESISTOR:
					outValue += "R: " + sigFigs.format(comp.resistance) + "\n";
					outValue += "A: " + sigFigs.format(comp.current)+ "\n";
					outValue += "V: " + sigFigs.format(comp.current * comp.resistance) + "\n";
					break;
				case BATTERY:
					outValue += "A: " + sigFigs.format(comp.current)+ "\n";
					outValue += "V: " + sigFigs.format(comp.voltageDif) + "\n";
					break;
				case WIRE:
					break;
				}
				if (comp.isLamp)
				{
					outValue += "Target A: " + sigFigs.format(comp.targetCurrent) + "+/-" + sigFigs.format(comp.targetMargin) + "\n";
					outValue += Math.abs(comp.targetCurrent - comp.current) < comp.targetMargin ? "On" : "Off";
				}
				font.draw(batch, outValue, 465, 90);
			}
		}
		batch.draw(cursor, cursorX * componentSize - circuitCamera.x, cursorY * componentSize - circuitCamera.y);
		ParticleSystem.draw(batch);
		batch.end();
		renderInventory(inventory);
		shapes.begin(ShapeType.Filled);
		shapes.setColor(Color.BLACK);
		shapes.rect(0, Gdx.graphics.getHeight() - 32, 640, 32);
		shapes.end();
		batch.begin();
		if(circuit.isSolved()) 
		{
			font.setColor(Color.GREEN);
			font.draw(batch, "Solved", Gdx.graphics.getWidth() - 64, Gdx.graphics.getHeight() - 12);
		}
		else
		{
			font.setColor(Color.RED);
			font.draw(batch, "In Progress", Gdx.graphics.getWidth() - 96, Gdx.graphics.getHeight() - 12);
		}
		font.draw(batch, "Lamps needed: " + circuit.goalLamps, 0, Gdx.graphics.getHeight() - 12);
		if(circuit.isSolved())
			font.draw(batch, "Press Escape to back out.", 130, Gdx.graphics.getHeight() - 12);
		batch.end();
	}

	private void renderInventory(Inventory inventory)
	{
		font.setColor(Color.WHITE);
		if (Gdx.input.isKeyJustPressed(Keys.TAB))
			showInventory = !showInventory;
		if (!showInventory)
			return;
		// Draw a background for the overlay
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		shapes.setColor(0.5f, 0.5f, 0.5f, 1);
		shapes.rect(0, 0, 464, 96);
		shapes.end();
		batch.begin();
		// Draw each icon followed by the quantity
		batch.draw(resistor, 0, 0, 32, 32);
		drawInventoryList(inventory.resistors, "Ohms               ", 0);
		batch.setColor(Color.BLACK);
		batch.draw(lamp, 0, 32, 32, 32);
		batch.setColor(Color.WHITE);
		drawInventoryList(inventory.lamps, "Amps Needed  ", 32);
		batch.draw(battery, 0, 64, 32, 32);
		drawInventoryList(inventory.batteries, "Volts                 ", 64);
		batch.end();
	}
	
	public void renderCredits()
	{
		batch.begin();
		batch.draw(credits, 0, 0);
		batch.end();
	}

	public void resetCircuitCamera()
	{
		circuitOffset.set(0, 0);
	}
	
	private int[] circuitAccumulator = new int[9];

	private void drawInventoryList(List<CircuitComponent> inventoryItems, String label, int height)
	{
		for (int i = 0; i < inventoryItems.size(); i++)
		{
			circuitAccumulator[(int) inventoryItems.get(i).getMainValue() - 1] += 1;
		}
		String value = label + "  ";
		for (int i = 0; i < circuitAccumulator.length; i++)
		{
			if(circuitAccumulator[i] != 0)
				value += (i + 1) + ":" + circuitAccumulator[i] + "    ";
			circuitAccumulator[i] = 0; // Reset the accumulator
		}
		font.draw(batch, value, 48, 24 + height, 32, Align.left, false);

	}

	private void draw(SpriteBatch batch, Texture t, float x, float y, float originX, float originY, float rotation)
	{
		batch.draw(t, x, y, originX, originY, t.getWidth(), t.getHeight(), 1, 1, rotation, 0, 0, t.getWidth(),
				t.getHeight(), false, false);
	}

	private void draw(SpriteBatch batch, TextureRegion t, float x, float y, float originX, float originY,
			float rotation)
	{
		batch.draw(t, x, y, originX, originY, t.getRegionWidth(), t.getRegionHeight(), 1, 1, rotation, false);
	}
}
