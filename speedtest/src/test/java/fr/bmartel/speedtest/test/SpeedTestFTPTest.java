/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Bertrand Martel
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package fr.bmartel.speedtest.test;

import fr.bmartel.speedtest.ISpeedTestListener;
import fr.bmartel.speedtest.SpeedTestError;
import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import net.jodah.concurrentunit.Waiter;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * FTP Speed test.
 *
 * @author Bertrand Martel
 */
public class SpeedTestFTPTest {

    /**
     * speed test mSocket object.
     */
    private SpeedTestSocket mSocket;

    /**
     * Waiter used for tests.
     */
    private Waiter mWaiter;

    /**
     * timestamp used to measure time interval.
     */
    private long mTimestamp;

    @Test
    public void downloadTest() throws TimeoutException {

        mSocket = new SpeedTestSocket();
        mSocket.setSocketTimeout(TestCommon.DEFAULT_SOCKET_TIMEOUT);

        final Waiter waiter = new Waiter();
        final Waiter waiter2 = new Waiter();

        final int packetSizeExpected = 1048576;

        mSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onDownloadFinished(final SpeedTestReport report) {
                SpeedTestUtils.checkSpeedTestResult(mSocket, waiter2, report.getTotalPacketSize(), packetSizeExpected,
                        report.getTransferRateBit(),
                        report.getTransferRateOctet(), true,
                        false);
                waiter2.resume();
            }

            @Override
            public void onDownloadProgress(final float percent, final SpeedTestReport report) {
                SpeedTestUtils.testReportNotEmpty(waiter, report, packetSizeExpected, true, false);
                waiter.assertTrue(percent >= 0 && percent <= 100);
                waiter.resume();
            }

            @Override
            public void onDownloadError(final SpeedTestError speedTestError, final String errorMessage) {
                waiter.fail(TestCommon.DOWNLOAD_ERROR_STR + speedTestError + " : " + errorMessage);
                waiter2.fail(TestCommon.DOWNLOAD_ERROR_STR + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadFinished(final SpeedTestReport report) {
                waiter.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onUploadFinished");
                waiter2.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onUploadFinished");
            }

            @Override
            public void onUploadError(final SpeedTestError speedTestError, final String errorMessage) {
                waiter.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onUploadError");
                waiter2.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onUploadError");
            }

            @Override
            public void onUploadProgress(final float percent, final SpeedTestReport report) {
                waiter.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onUploadProgress");
                waiter2.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onUploadProgress");
            }

            @Override
            public void onInterruption() {

            }
        });

        mSocket.startFtpDownload(TestCommon.FTP_SERVER_HOST, TestCommon.FTP_SERVER_URI);

        waiter.await(TestCommon.WAITING_TIMEOUT_DEFAULT_SEC, TimeUnit.SECONDS);

        waiter2.await(TestCommon.WAITING_TIMEOUT_VERY_LONG_OPERATION, TimeUnit.SECONDS);

        mSocket.clearListeners();
    }

    @Test
    public void uploadTest() throws TimeoutException {

        mSocket = new SpeedTestSocket();
        mSocket.setSocketTimeout(TestCommon.DEFAULT_SOCKET_TIMEOUT);

        final Waiter waiter = new Waiter();
        final Waiter waiter2 = new Waiter();

        final int packetSizeExpected = 1000000;

        mSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onDownloadFinished(final SpeedTestReport report) {
                waiter.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onDownloadFinished");
                waiter2.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onDownloadFinished");

            }

            @Override
            public void onDownloadProgress(final float percent, final SpeedTestReport report) {
                waiter.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onDownloadProgress");
                waiter2.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onDownloadProgress");
            }

            @Override
            public void onDownloadError(final SpeedTestError speedTestError, final String errorMessage) {
                waiter.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onDownloadError");
                waiter2.fail(TestCommon.UPLOAD_ERROR_STR + " : shouldnt be in onDownloadError");
            }

            @Override
            public void onUploadFinished(final SpeedTestReport report) {
                SpeedTestUtils.checkSpeedTestResult(mSocket, waiter2, report.getTotalPacketSize(), packetSizeExpected,
                        report.getTransferRateBit(),
                        report.getTransferRateOctet(), false,
                        false);
                waiter2.resume();
            }

            @Override
            public void onUploadError(final SpeedTestError speedTestError, final String errorMessage) {
                waiter.fail(TestCommon.UPLOAD_ERROR_STR + speedTestError + " : " + errorMessage);
                waiter2.fail(TestCommon.UPLOAD_ERROR_STR + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadProgress(final float percent, final SpeedTestReport report) {
                SpeedTestUtils.testReportNotEmpty(waiter, report, packetSizeExpected, true, false);
                waiter.assertTrue(percent >= 0 && percent <= 100);
                waiter.resume();
            }

            @Override
            public void onInterruption() {

            }
        });

        mSocket.startFtpUpload(TestCommon.FTP_SERVER_HOST, SpeedTestUtils.getFTPUploadUri(), packetSizeExpected);

        waiter.await(TestCommon.WAITING_TIMEOUT_DEFAULT_SEC, TimeUnit.SECONDS);

        waiter2.await(TestCommon.WAITING_TIMEOUT_VERY_LONG_OPERATION, TimeUnit.SECONDS);

        mSocket.clearListeners();
    }

    @Test
    public void downloadWithReportIntervalTest() throws TimeoutException {

        mSocket = new SpeedTestSocket();
        mSocket.setSocketTimeout(TestCommon.DEFAULT_SOCKET_TIMEOUT);

        final int requestInterval = 500;

        mSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onDownloadFinished(final SpeedTestReport report) {
                mWaiter.resume();
            }

            @Override
            public void onDownloadProgress(final float percent, final SpeedTestReport report) {
                checkReportIntervalValue(requestInterval);
            }

            @Override
            public void onDownloadError(final SpeedTestError speedTestError, final String errorMessage) {
                mWaiter.fail("unexpected error in onDownloadError : " + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadFinished(final SpeedTestReport report) {
                mWaiter.resume();
            }

            @Override
            public void onUploadError(final SpeedTestError speedTestError, final String errorMessage) {
                mWaiter.fail("unexpected error in onUploadError : " + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadProgress(final float percent, final SpeedTestReport report) {
                checkReportIntervalValue(requestInterval);
            }

            @Override
            public void onInterruption() {
                mWaiter.fail("shouldnt be in onInterruption");
            }
        });

        mWaiter = new Waiter();
        mTimestamp = 0;
        mSocket.startFtpUpload(TestCommon.FTP_SERVER_HOST, SpeedTestUtils.getFTPUploadUri(),
                requestInterval);

        mWaiter.await(TestCommon.WAITING_TIMEOUT_VERY_LONG_OPERATION, TimeUnit.SECONDS);

        mWaiter = new Waiter();
        mTimestamp = 0;
        mSocket.startFtpDownload(TestCommon.FTP_SERVER_HOST, TestCommon.FTP_SERVER_URI,
                requestInterval);

        mWaiter.await(TestCommon.WAITING_TIMEOUT_VERY_LONG_OPERATION, TimeUnit.SECONDS);

        mSocket.clearListeners();
    }

    /**
     * Compare report interval value measured to actual one.
     *
     * @param requestInterval
     */
    private void checkReportIntervalValue(final int requestInterval) {
        final long currentTimestamp = System.currentTimeMillis();
        if (mTimestamp > 0) {
            final long diff = currentTimestamp - mTimestamp;
            if (diff < (requestInterval - TestCommon.OFFSET_REPORT_INTERVAL) ||
                    diff > (requestInterval + TestCommon.OFFSET_REPORT_INTERVAL)) {
                mWaiter.fail("expected " + requestInterval + " | current val : " +
                        (currentTimestamp - mTimestamp));
            }
        }
        mTimestamp = currentTimestamp;
    }

    @Test
    public void fixDurationTest() throws TimeoutException {

        mSocket = new SpeedTestSocket();
        mSocket.setSocketTimeout(TestCommon.DEFAULT_SOCKET_TIMEOUT);

        final int packetSizeExpected = 50000000;

        final int duration = 2000;

        mSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onDownloadFinished(final SpeedTestReport report) {
                mWaiter.resume();
            }

            @Override
            public void onDownloadProgress(final float percent, final SpeedTestReport report) {

            }

            @Override
            public void onDownloadError(final SpeedTestError speedTestError, final String errorMessage) {
                mWaiter.fail("unexpected error in onDownloadError : " + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadFinished(final SpeedTestReport report) {
                mWaiter.resume();
            }

            @Override
            public void onUploadError(final SpeedTestError speedTestError, final String errorMessage) {
                mWaiter.fail("unexpected error in onUploadError : " + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadProgress(final float percent, final SpeedTestReport report) {
            }

            @Override
            public void onInterruption() {
                mWaiter.resume();
            }
        });
        mWaiter = new Waiter();
        mSocket.startFtpFixedUpload(TestCommon.FTP_SERVER_HOST, SpeedTestUtils.getFTPUploadUri(),
                packetSizeExpected, duration);

        mWaiter.await(duration + TestCommon.FIXED_DURATION_OFFSET, TimeUnit.MILLISECONDS);

        mWaiter = new Waiter();

        mSocket.startFtpFixedDownload(TestCommon.FTP_SERVER_HOST, TestCommon.FTP_SERVER_URI_LARGE_FILE,
                duration);

        mWaiter.await(duration + TestCommon.FIXED_DURATION_OFFSET, TimeUnit.MILLISECONDS);

        mSocket.clearListeners();
    }


    @Test
    public void fixDurationWithReportIntervalTest() throws TimeoutException {

        mSocket = new SpeedTestSocket();
        mSocket.setSocketTimeout(TestCommon.DEFAULT_SOCKET_TIMEOUT);

        final int packetSizeExpected = 50000000;

        final int duration = 2000;
        final int requestInterval = 500;

        mSocket.addSpeedTestListener(new ISpeedTestListener() {
            @Override
            public void onDownloadFinished(final SpeedTestReport report) {
                mWaiter.resume();
            }

            @Override
            public void onDownloadProgress(final float percent, final SpeedTestReport report) {
                checkReportIntervalValue(requestInterval);
            }

            @Override
            public void onDownloadError(final SpeedTestError speedTestError, final String errorMessage) {
                mWaiter.fail("unexpected error in onDownloadError : " + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadFinished(final SpeedTestReport report) {
                mWaiter.resume();
            }

            @Override
            public void onUploadError(final SpeedTestError speedTestError, final String errorMessage) {
                mWaiter.fail("unexpected error in onUploadError : " + speedTestError + " : " + errorMessage);
            }

            @Override
            public void onUploadProgress(final float percent, final SpeedTestReport report) {
                checkReportIntervalValue(requestInterval);
            }

            @Override
            public void onInterruption() {
                mWaiter.resume();
            }
        });

        mWaiter = new Waiter();
        mTimestamp = 0;
        mSocket.startFtpFixedUpload(TestCommon.FTP_SERVER_HOST, SpeedTestUtils.getFTPUploadUri(),
                packetSizeExpected, duration, requestInterval);

        mWaiter.await(duration + TestCommon.FIXED_DURATION_OFFSET, TimeUnit.MILLISECONDS);

        mWaiter = new Waiter();
        mTimestamp = 0;
        mSocket.startFtpFixedDownload(TestCommon.FTP_SERVER_HOST, TestCommon.FTP_SERVER_URI_LARGE_FILE,
                duration, requestInterval);

        mWaiter.await(duration + TestCommon.FIXED_DURATION_OFFSET, TimeUnit.MILLISECONDS);

        mSocket.clearListeners();
    }
}
