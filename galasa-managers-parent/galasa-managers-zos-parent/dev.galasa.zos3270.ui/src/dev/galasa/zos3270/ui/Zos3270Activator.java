package dev.galasa.zos3270.ui;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Zos3270Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "dev.galasa.zos3270.ui"; //$NON-NLS-1$

	// The shared instance
	private static Zos3270Activator plugin;
	
	/**
	 * The constructor
	 */
	public Zos3270Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Zos3270Activator getDefault() {
		return plugin;
	}
	
	   /**
     * Log a throwable
     * 
     * @param e
     */
    public static void log(Throwable e) {
        log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, "Error", e)); //$NON-NLS-1$
    }

    /**
     * Log a status
     * 
     * @param status
     */
    public static void log(IStatus status) {
        ILog log = plugin.getLog();
        if (log != null) {
            log.log(status);
        }
    }

    /**
     * 
     * @return - plugin ID
     */
    public static String getPluginId() {
        return PLUGIN_ID;
    }


}