package com.rnd.report;

import android.util.Log;

import com.rnd.room.ImpressionEntity;

import java.util.List;

public class ReportUtils {

    public static ReportData generateReport(long visitors,long happyW,long sadW,long neutralW,long maleW,long child,long teen,long adult,long senior) {
        ReportData data = new ReportData();

        data.totalVisitors = (int) visitors;

        data.happyPercent = (int)((happyW * 100.0) / data.totalVisitors);
        data.neutralPercent = (int)((neutralW * 100.0) / data.totalVisitors);
        data.sadPercent = (int)((sadW * 100.0) / data.totalVisitors);

        // النوع
        data.malePercent = (int)((maleW * 100.0) / data.totalVisitors);
        if(data.totalVisitors>0){
            data.femalePercent = 100 - data.malePercent;
        }else{
            data.femalePercent = 0;
        }

        // الأعمار
        data.childCount = (int) child;
        data.teenCount = (int) teen;
        data.adultCount = (int) adult;
        data.seniorCount = (int) senior;
        return data;
    }

    public static void saveReportLocally(ReportData data) {
        // ممكن تحفظه كـ JSON أو تخزنه في Room أو تطبعه في log
    }
}

