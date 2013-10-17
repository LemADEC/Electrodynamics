package dark.assembly.common.armbot.command;

import com.builtbroken.common.science.units.UnitHelper;

import universalelectricity.core.vector.Vector3;
import dark.api.al.coding.IArmbot;
import dark.api.al.coding.ILogicDevice;
import dark.api.al.coding.IDeviceTask.TaskType;
import dark.api.al.coding.args.ArgumentData;
import dark.assembly.common.armbot.TaskBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class CommandIdle extends TaskBase
{

    /** The amount of time in which the machine will idle. */
    public int idleTime = 80;
    private int totalIdleTime = 80;

    public CommandIdle()
    {
        super("wait", TaskType.DEFINEDPROCESS);
        this.defautlArguments.add(new ArgumentData("idleTime", 20));
    }

    @Override
    public ProcessReturn onMethodCalled(World world, Vector3 location, ILogicDevice armbot)
    {
        super.onMethodCalled(world, location, armbot);

        if (UnitHelper.tryToParseInt(this.getArg("idleTime")) > 0)
        {
            this.totalIdleTime = this.idleTime = UnitHelper.tryToParseInt(this.getArg("idleTime"));
            return ProcessReturn.CONTINUE;
        }
        return ProcessReturn.ARGUMENT_ERROR;
    }

    @Override
    public ProcessReturn onUpdate()
    {
        if (this.idleTime > 0)
        {
            this.idleTime--;
            return ProcessReturn.CONTINUE;
        }
        return ProcessReturn.DONE;
    }

    @Override
    public TaskBase loadProgress(NBTTagCompound taskCompound)
    {
        super.loadProgress(taskCompound);
        this.idleTime = taskCompound.getInteger("idleTime");
        this.totalIdleTime = taskCompound.getInteger("idleTotal");
        return this;
    }

    @Override
    public NBTTagCompound saveProgress(NBTTagCompound taskCompound)
    {
        super.saveProgress(taskCompound);
        taskCompound.setInteger("idleTime", this.idleTime);
        taskCompound.setInteger("idleTotal", this.totalIdleTime);
        return taskCompound;
    }

    @Override
    public String toString()
    {
        return super.toString() + " " + Integer.toString(this.totalIdleTime);
    }

    @Override
    public TaskBase clone()
    {
        return new CommandIdle();
    }

    @Override
    public boolean canUseTask(ILogicDevice device)
    {
        return true;
    }

}
