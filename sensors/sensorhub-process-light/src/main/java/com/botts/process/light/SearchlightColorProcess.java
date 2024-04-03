package com.botts.process.light;

import net.opengis.swe.v20.Text;
import org.sensorhub.api.ISensorHub;
import org.sensorhub.api.command.CommandData;
import org.sensorhub.api.datastore.command.CommandStreamFilter;
import org.sensorhub.api.processing.OSHProcessInfo;
import org.sensorhub.impl.processing.ISensorHubProcess;
import org.sensorhub.impl.system.CommandStreamTransactionHandler;
import org.sensorhub.impl.system.SystemDatabaseTransactionHandler;
import org.sensorhub.utils.Async;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.process.ExecutableProcessImpl;
import org.vast.process.ProcessInfo;
import org.vast.swe.SWEHelper;

import java.util.concurrent.TimeoutException;

public class SearchlightColorProcess extends ExecutableProcessImpl implements ISensorHubProcess {
    protected static final Logger logger = LoggerFactory.getLogger(SearchlightColorProcess.class);

    public static final OSHProcessInfo INFO = new OSHProcessInfo("pibot:searchlight", "Searchlight", null, SearchlightColorProcess.class);
    public static final String SYSTEM_UID_PARAM = "systemUID";
    public static final String OUTPUT_NAME_PARAM = "inputName";
    String systemUid;
    final Text systemUidParam;
    Text inputNameParam;
    String inputName;
    CommandStreamTransactionHandler commandHandler;
    ISensorHub hub;
    public SearchlightColorProcess() {

        super(INFO);

        SWEHelper fac = new SWEHelper();

        // params
        systemUidParam = fac.createText()
                .definition(SWEHelper.getPropertyUri("SystemUID"))
                .label("Producer Unique ID")
                .build();
        paramData.add(SYSTEM_UID_PARAM, systemUidParam);

        inputNameParam = fac.createText()
                .definition(SWEHelper.getPropertyUri("OutputName"))
                .label("Output Name")
                .build();
        paramData.add(OUTPUT_NAME_PARAM, inputNameParam);

    }

    @Override
    public void notifyParamChange(){

        systemUid = systemUidParam.getData().getStringValue();
        inputName = inputNameParam.getData().getStringValue();

        if(systemUid !=null && inputName != null){
            try{
                // wait here to make sure parent system and control stream have been registered.
                // needed to handle case where system is being registered concurrently.
                Async.waitForCondition(this::checkForControlStream, 500, 10000);
            }catch(TimeoutException e){
                if(processInfo == null){
                    throw new IllegalStateException("System "+ systemUid+ " not found", e);

                }else {
                    throw new IllegalStateException("System "+ systemUid+ " is missing input "+ inputName, e);
                }
            }
        }
    }

    private boolean checkForControlStream() {
        var db = hub.getDatabaseRegistry().getFederatedDatabase();
        var sysEntry = db.getSystemDescStore().getCurrentVersionEntry(systemUid);
        if (sysEntry == null)
            return false;

        // set process info
        ProcessInfo instanceInfo = new ProcessInfo(
                processInfo.getUri(),
                sysEntry.getValue().getName(),
                processInfo.getDescription(),
                processInfo.getImplementationClass());
        this.processInfo = instanceInfo;

        // get control stream corresponding to inputName
        db.getCommandStreamStore().selectEntries(new CommandStreamFilter.Builder()
                        .withSystems(sysEntry.getKey().getInternalID())
                        .withControlInputNames(inputName)
                        .withCurrentVersion()
                        .build())
                .forEach(entry -> {
                    // add input with same schema as control stream
                    inputData.add(
                            entry.getValue().getControlInputName(),
                            entry.getValue().getRecordStructure().copy());

                    // also get handle to handler used to send commands
                    var systemHandler = new SystemDatabaseTransactionHandler(hub.getEventBus(), hub.getDatabaseRegistry().getFederatedDatabase());
                    commandHandler = systemHandler.getCommandStreamHandler(entry.getKey().getInternalID());
                });

        return !inputData.isEmpty();
    }

    @Override
    public synchronized void stop()
    {
        if (started)
        {
            started = false;
            getLogger().debug("Disconnected from system '{}'", systemUid);
        }
    }


    @Override
    public void execute() {
        logger.debug("Processing event");

        var cmdData = inputData.getComponent(0).getData();
        var cmd = new CommandData.Builder()
                .withCommandStream(commandHandler.getCommandStreamKey().getInternalID())
                .withParams(cmdData)
                .withSender(getProcessInfo().getUri()+ "#"+ getInstanceName())
                .build();

        commandHandler.submitCommand(0, cmd).thenAccept(s->{

        });
        logger.debug("Processed event");
    }


    @Override
    public boolean needSync(){
        return false;
    }
    @Override
    public void setParentHub(ISensorHub hub) {
        this.hub = hub;
    }
}
