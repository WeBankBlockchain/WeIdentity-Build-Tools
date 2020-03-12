jQuery.extend(jQuery.fn.dataTableExt.oSort, {
    "operation-column-pre": function (a) {
    	var div = $(a).html();
        return div;
    },
    "operation-column-asc": function (a, b) {                //正序排序引用方法
        return ((a < b) ? -1 : ((a > b) ? 1 : 0));
    },
    "operation-column-desc": function (a, b) {                //倒序排序引用方法
        return ((a < b) ? 1 : ((a > b) ? -1 : 0));
    }
});