package net.minecraft.src.eui.api;


public interface ISteamConsumer
{
	/**
	 * onRecieveElectricity is called whenever a Universal Electric conductor sends a packet of electricity to the consumer (which is this block).
	 * @param watts - The amount of watts this block received.
	 * @param side - The side of the block in which the electricity came from.
	 * @return watt - The amount of rejected power to be sent back into the conductor
	 */
	public int onReceiveSteam(int watts, byte side);
	// 1 unit of steam is = to 7132.6gal(27m^3) at 1ATM 424*F(200*C)
	/**
	 * You can use this to check if a wire can connect to this UE consumer to properly render the graphics
	 * @param side - The side in which the electricity is coming from.
	 * @return Returns true or false if this consumer can receive electricity at this given tick or moment.
	 */
	public boolean canRecieveSteam(byte side);
	
	/**
	 * @return Return the stored electricity in this consumer. Called by conductors to spread electricity to this unit.
	 */
	public int getStoredSteam();
	
	/**
	 * @return Return the maximum amount of stored electricity this consumer can get.
	 */
	public int getSteamCapacity();
}
