
package com.kingbright.fil2explorer.file;

public interface FileScanListener {
    public void onScanStart();

    public void onScanSuccessed();

    public void onScanCanceled();

    public void onScanFailed();

    public void onScanRunning();
}
