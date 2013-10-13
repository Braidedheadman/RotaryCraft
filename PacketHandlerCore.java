/*******************************************************************************
 * @author Reika Kalseki
 * 
 * Copyright 2013
 * 
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 ******************************************************************************/
package Reika.RotaryCraft;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Random;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import Reika.DragonAPI.DragonAPICore;
import Reika.DragonAPI.Auxiliary.PacketTypes;
import Reika.DragonAPI.Libraries.Java.ReikaJavaLibrary;
import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
import Reika.DragonAPI.Libraries.Registry.ReikaParticleHelper;
import Reika.RotaryCraft.Base.TileEntityAimedCannon;
import Reika.RotaryCraft.Base.TileEntityLaunchCannon;
import Reika.RotaryCraft.Items.Tools.ItemJetPackChest;
import Reika.RotaryCraft.ModInterface.TileEntityPneumaticEngine;
import Reika.RotaryCraft.Registry.PacketRegistry;
import Reika.RotaryCraft.Registry.SoundRegistry;
import Reika.RotaryCraft.TileEntities.TileEntityDisplay;
import Reika.RotaryCraft.TileEntities.TileEntityItemCannon;
import Reika.RotaryCraft.TileEntities.TileEntityMusicBox;
import Reika.RotaryCraft.TileEntities.TileEntityPlayerDetector;
import Reika.RotaryCraft.TileEntities.TileEntityProjector;
import Reika.RotaryCraft.TileEntities.TileEntityScaleableChest;
import Reika.RotaryCraft.TileEntities.TileEntityScreen;
import Reika.RotaryCraft.TileEntities.TileEntityTerraformer;
import Reika.RotaryCraft.TileEntities.TileEntityVacuum;
import Reika.RotaryCraft.TileEntities.TileEntityWinder;
import Reika.RotaryCraft.TileEntities.Auxiliary.TileEntityHeater;
import Reika.RotaryCraft.TileEntities.Auxiliary.TileEntityMirror;
import Reika.RotaryCraft.TileEntities.Farming.TileEntitySpawnerController;
import Reika.RotaryCraft.TileEntities.Production.TileEntityBorer;
import Reika.RotaryCraft.TileEntities.Production.TileEntityEngine;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntityAdvancedGear;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntityGearBevel;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntityMultiClutch;
import Reika.RotaryCraft.TileEntities.Transmission.TileEntitySplitter;
import Reika.RotaryCraft.TileEntities.Weaponry.TileEntityContainment;
import Reika.RotaryCraft.TileEntities.Weaponry.TileEntityForceField;
import Reika.RotaryCraft.TileEntities.Weaponry.TileEntitySonicWeapon;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;


public abstract class PacketHandlerCore implements IPacketHandler {

	private TileEntityBorer borer;
	private TileEntityGearBevel gbevel;
	private TileEntitySplitter splitter;
	private TileEntitySpawnerController spawner;
	private TileEntityPlayerDetector detector;
	private TileEntityHeater heater;
	private TileEntityAdvancedGear adv;
	private TileEntityLaunchCannon cannon;
	private TileEntitySonicWeapon sonic;
	private TileEntityForceField force;
	private TileEntityScaleableChest chest;
	private TileEntityMusicBox music;
	private TileEntityVacuum vac;
	private TileEntityWinder winder;
	private TileEntityProjector proj;
	private TileEntityContainment cont;
	private TileEntityScreen screen;
	private TileEntityItemCannon icannon;
	private TileEntityMirror mirror;
	private TileEntityAimedCannon aimed;
	private TileEntityEngine engine;
	private TileEntityDisplay display;
	private TileEntityMultiClutch redgear;
	private TileEntityTerraformer terra;
	private TileEntityPneumaticEngine eng;

	protected PacketRegistry pack;
	protected PacketTypes packetType;

	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		this.process(packet, (EntityPlayer)player);
	}

	public abstract void process(Packet250CustomPayload packet, EntityPlayer ep);

	public void handleData(Packet250CustomPayload packet, World world, EntityPlayer ep) {
		DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(packet.data));
		int control = Integer.MIN_VALUE;
		int len;
		int[] data = new int[0];
		long longdata = 0;
		float floatdata = 0;
		int x,y,z;
		boolean readinglong = false;
		String stringdata = null;
		//System.out.print(packet.length);
		try {
			//ReikaJavaLibrary.pConsole(inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt()+":"+inputStream.readInt());
			packetType = PacketTypes.getPacketType(inputStream.readInt());
			switch(packetType) {
			case SOUND:
				SoundRegistry.playSoundPacket(inputStream);
				return;
			case STRING:
				control = inputStream.readInt();
				pack = PacketRegistry.getEnum(control);
				stringdata = Packet.readString(inputStream, Short.MAX_VALUE);
				break;
			case DATA:
				control = inputStream.readInt();
				pack = PacketRegistry.getEnum(control);
				len = pack.getNumberDataInts();
				data = new int[len];
				readinglong = pack.isLongPacket();
				if (!readinglong) {
					for (int i = 0; i < len; i++)
						data[i] = inputStream.readInt();
				}
				else
					longdata = inputStream.readLong();
				break;
			case UPDATE:
				control = inputStream.readInt();
				pack = PacketRegistry.getEnum(control);
				break;
			case FLOAT:
				control = inputStream.readInt();
				pack = PacketRegistry.getEnum(control);
				floatdata = inputStream.readFloat();
				break;
			}
			x = inputStream.readInt();
			y = inputStream.readInt();
			z = inputStream.readInt();
		}
		catch (IOException e) {
			e.printStackTrace();
			return;
		}
		switch (pack) {
		case BORER: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d   %d", control, data));
			borer = (TileEntityBorer)world.getBlockTileEntity(x, y, z);
			if (borer != null) {
				if (control == 2) {
					borer.mode = data[0];
				}
				if (control == 3) {
					borer.step = 1;
				}
				if (control == 1) {
					//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d", data));
					if (borer.drops) {
						borer.drops = false;
					}
					else {
						borer.drops = true;
					}
				}
				if (control == 0) {
					if (data[0] > 0 && data[0] < 100) {
						int roworld = data[0]/7;
						int cols = data[0]-roworld*7;
						borer.cutShape[cols][roworld] = false;
						//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d -> row %d col %d", data, roworld, cols));
					}
					if (data[0] < 0 && data[0] > -100) {
						int roworld = -data[0]/7;
						int cols = -data[0]-roworld*7;
						borer.cutShape[cols][roworld] = true;
						//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d -> row %d col %d", data, roworld, cols));
					}
					if (data[0] == 100) {
						borer.cutShape[0][0] = false;
						//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d -> row %d col %d", data, 0, 0));
					}
					if (data[0] == -100) {
						borer.cutShape[0][0] = true;
						//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d -> row %d col %d", data, 0, 0));
					}
				}

			}
		}
		break;
		case BEVEL: {
			gbevel = (TileEntityGearBevel)world.getBlockTileEntity(x, y, z);
			gbevel.direction = data[0];
		}
		break;
		case SPLITTER: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", control, data));
			splitter = (TileEntitySplitter)world.getBlockTileEntity(x, y, z);
			if (control == 6) {
				splitter.splitmode = data[0];
			}
		}
		break;
		case SPAWNER: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", control, data));
			spawner = (TileEntitySpawnerController)world.getBlockTileEntity(x, y, z);
			if (data[0] == -1) {
				spawner.disable = true;
			}
			else {
				spawner.disable = false;
				spawner.setDelay = data[0];
			}
		}
		break;
		case DETECTOR: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", control, data));
			detector = (TileEntityPlayerDetector)world.getBlockTileEntity(x, y, z);
			detector.selectedrange = data[0];
		}
		break;
		case HEATER: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", control, data));
			heater = (TileEntityHeater)world.getBlockTileEntity(x, y, z);
			heater.setTemperature = data[0];
		}
		break;
		case CVT: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", control, data));
			adv = (TileEntityAdvancedGear)world.getBlockTileEntity(x, y, z);
			adv.ratio = data[0];
		}
		break;
		case CANNON: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", control, data));
			cannon = (TileEntityLaunchCannon)world.getBlockTileEntity(x, y, z);
			if (data[0] == 0) {
				if (control == 12) {
					cannon.phi = data[1];
				}
				if (control == 13) {
					if (data[1] > cannon.getMaxTheta())
						cannon.theta = cannon.getMaxTheta();
					else
						cannon.theta = data[1];
				}
				if (control == 14) {
					if (data[1] > cannon.getMaxLaunchVelocity())
						cannon.velocity = cannon.getMaxLaunchVelocity();
					else
						cannon.velocity = data[1];
				}
			}
			else {
				if (control == 12) {
					cannon.target[0] = data[1];
				}
				if (control == 13) {
					cannon.target[1] = data[1];
				}
				if (control == 14) {
					cannon.target[2] = data[1];
				}
				double dx = cannon.target[0]-cannon.xCoord;
				double dz = cannon.target[2]-cannon.zCoord;
				double dd = ReikaMathLibrary.py3d(dx, 0, dz);
				if (dd > cannon.getMaxLaunchDistance()) {
					cannon.target[0] = cannon.xCoord;
					cannon.target[1] = 512;
					cannon.target[2] = cannon.zCoord;
				}
			}
		}
		break;
		case SONIC: {
			sonic = (TileEntitySonicWeapon)world.getBlockTileEntity(x, y, z);
			if (control == 15) {
				sonic.setpitch = longdata;
			}
			if (control == 16) {
				sonic.setvolume = longdata;
			}
		}
		break;
		case FORCE: {
			force = (TileEntityForceField)world.getBlockTileEntity(x, y, z);
			force.setRange = data[0];
		}
		break;
		case CHEST: {
			chest = (TileEntityScaleableChest)world.getBlockTileEntity(x, y, z);
			int oldpg = chest.page;
			chest.page = data[0];
			//ep.openGui(RotaryCraft.instance, 0, chest.worldObj, chest.xCoord, chest.yCoord, chest.zCoord);
			break;
		}
		case COIL:
			adv = (TileEntityAdvancedGear)world.getBlockTileEntity(x, y, z);
			if (control == 19) {
				adv.releaseOmega = data[0];
			}
			if (control == 20) {
				adv.releaseTorque = data[0];
			}
			break;
		case MUSIC:
			music = (TileEntityMusicBox)world.getBlockTileEntity(x, y, z);
			switch(control) {
			case 21:
				music.chooseNoteType(data[0]);
				break;
			case 22:
				music.setEntryData(data[0], data[1], data[2], data[3]);
				music.addNote();
				break;
			case 23:
				music.addNote();
				music.backSpace();
				break;
			case 24:
				music.clearMusic();
				break;
			case 25:
				music.clearChannel(data[0]);
				break;
			case 26:
				music.save();
				break;
			case 27:
				music.read();
				break;
			case 28:
				music.loadDemo();
				music.isOneTimePlaying = true;
				break;
			}
			break;
		case VACUUM:
			vac = (TileEntityVacuum)world.getBlockTileEntity(x, y, z);
			vac.spawnXP();
			break;
		case WINDER:
			winder = (TileEntityWinder)world.getBlockTileEntity(x, y, z);
			if (winder.winding) {
				winder.winding = false;
			}
			else {
				winder.winding = true;
			}
			winder.iotick = 512;
			break;
		case PROJECTOR:
			proj = (TileEntityProjector)world.getBlockTileEntity(x, y, z);
			proj.cycleInv();
			break;
		case CONTAINMENT:
			cont = (TileEntityContainment)world.getBlockTileEntity(x, y, z);
			cont.setRange = data[0];
			break;
		case ITEMCANNON: {
			//ModLoader.getMinecraftInstance().thePlayer.addChatMessage(String.format("%d  %d", control, data));
			icannon = (TileEntityItemCannon)world.getBlockTileEntity(x, y, z);
			if (control == 33) {
				icannon.target[0] = data[0];
			}
			if (control == 34) {
				icannon.target[1] = data[0];
			}
			if (control == 35) {
				icannon.target[2] = data[0];
			}
			break;
		}
		case MIRROR:
			mirror = (TileEntityMirror)world.getBlockTileEntity(x, y, z);
			mirror.breakMirror(world, x, y, z);
			break;
		case SAFEPLAYER:
			aimed = (TileEntityAimedCannon)world.getBlockTileEntity(x, y, z);
			aimed.removePlayerFromWhiteList(stringdata);
			break;
		case ENGINEBACKFIRE:
			engine = (TileEntityEngine)world.getBlockTileEntity(x, y, z);
			if (engine != null)
				engine.backFire(world, x, y, z);
			break;
		case MUSICPARTICLE:
			Random rand = new Random();
			music = (TileEntityMusicBox)world.getBlockTileEntity(x, y, z);
			world.spawnParticle("note", x+0.2+rand.nextDouble()*0.6, y+1.2, z+0.2+rand.nextDouble()*0.6, rand.nextDouble(), 0.0D, 0.0D); //activeNote/24D
			break;
		case DISPLAY:
			display = (TileEntityDisplay)world.getBlockTileEntity(x, y, z);
			if (control == 40)
				display.setFullMessage(stringdata);
			if (control == 41)
				display.saveToFile();
			if (control == 42)
				display.readFromFile();
			break;
		case CHESTRELOAD:
			//ep.closeScreen();
			//ep.openGui(RotaryCraft.instance, GuiRegistry.MACHINE.ordinal(), world, x, y, z);
			//ReikaJavaLibrary.pConsole("Opening gui for "+ep+" on "+FMLCommonHandler.instance().getEffectiveSide());
			break;
		case REDGEAR:
			redgear = (TileEntityMultiClutch)world.getBlockTileEntity(x, y, z);
			redgear.setSideOfState(data[0], data[1]);
			break;
		case TERRAFORMER:
			terra = (TileEntityTerraformer)world.getBlockTileEntity(x, y, z);
			terra.setTarget(BiomeGenBase.biomeList[data[0]]);
			break;
		case PNEUMATIC:
			eng = (TileEntityPneumaticEngine)world.getBlockTileEntity(x, y, z);
			if (control == 50)
				eng.decrement();
			if (control == 51)
				eng.increment();
			break;
		case JETPACK:
			boolean move = floatdata > 100;
			if (move) {
				floatdata -= 100;
				float retruster = 0.3F;
				float forwardpower = floatdata * retruster * 2.0F;
				if (forwardpower > 0.0F) {
					ep.moveFlying(0.0F, forwardpower, 2F);
				}
			}
			ep.motionY += floatdata*4.25;
			if (ep.motionY > 0.6)
				ep.motionY = 0.6;
			double vx = ep.motionX;
			double vz = ep.motionZ;
			if (DragonAPICore.isOnActualServer()) {
				ep.velocityChanged = true;
			}
			//PacketDispatcher.sendPacketToAllInDimension(new Packet28EntityVelocity(ep), world.provider.dimensionId);
			//ReikaJavaLibrary.pConsole(ep.motionY);
			ep.fallDistance = 0.0F;
			ep.distanceWalkedModified = 0.0F;
			if (!ep.capabilities.isCreativeMode) {
				ItemStack jet = ep.getCurrentArmor(2);
				ItemJetPackChest i = (ItemJetPackChest)jet.getItem();
				i.use(jet, 4);
			}
			break;
		case FERTILIZER:
			if (world.isRemote) {
				ReikaParticleHelper.BONEMEAL.spawnAroundBlock(world, x, y, z, 4);
			}
			break;
		case GRAVELGUN:
			ReikaJavaLibrary.pConsole(x+", "+y+", "+z);
			ReikaParticleHelper.EXPLODE.spawnAroundBlock(world, x, y, z, 1);
			world.playSoundEffect(x, y, z, "random.explode", 1, 1F);
			break;
		}
	}
}
