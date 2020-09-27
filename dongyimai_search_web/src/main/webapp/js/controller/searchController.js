app.controller("searchContorller",function($scope,$location,searchService) {

    //搜索对象
    $scope.resultMap = {};
    $scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,
        "pageSize":10, 'sortField':'','sort':''};//搜索对象

    //主页页面跳转页面
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        $scope.search();
    }

    //搜索
    $scope.search=function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap = response;//搜索查询的结果
            //分成页码
            buildPageLabel();
        })
    }

    //添加一个搜索项
    $scope.addSearchItem=function (key,value) {
        //初始化当前页为1
        $scope.searchMap.pageNo=1;
        if(key=='category' || key=='brand'||key=='price') {//如果点击的是分类或者是品牌
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();
    }

    //移除复合搜索条件
    $scope.removeSearchItem = function (key) {
        //初始化当前页为1
        $scope.searchMap.pageNo=1;
        if(key == "category" || key == "brand" || key=='price'){//如果是分类或者品牌
            $scope.searchMap[key]="";
        }else{//如果是规格
            delete $scope.searchMap.spec[key];//delete 操作符用于删除对象的某个属性。
        }
        $scope.search();
    }


    //构建分页标签  自己的方法
    buildPageLabel = function () {
        $scope.pageLabel = [];//新增分页属性
        var maxPageNo = $scope.resultMap.totalPages;//得到最后的页码
        //开始页码
        var firstPage = 1;
        //截止页码
        var lastPage = maxPageNo;

        //省略号
        $scope.firstDot = true;
        $scope.lastDot  = true;

        //如果总页数大于5页，显示部分页码
        if ($scope.resultMap.totalPages > 5) {
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false;//前面省略号关闭
            } else if ($scope.searchMap.pageNo >= lastPage - 2) {
                firstPage = maxPageNo - 4;
                $scope.lastDot  = false;//后面省略号关闭
            } else {//显示当前页码为中心的5页
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        }else{
            $scope.firstDot=false;//前面无点
            $scope.lastDot=false;//后边无点
        }

        //循环产生页码标签
        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }



    //根据页码查询
    $scope.queryByPage = function (pageNo) {
        //页码验证
        if(pageNo <1 || pageNo>$scope.resultMap.totalPages){
            return ;
        }

        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }


    //判断当前页为第一页
    $scope.isTopPage = function () {
        if($scope.searchMap.pageNo == 1){
            return true;
        }else{
            return false;
        }
    }

    //判断指定页码是否是当前页
    $scope.ispage=function (p) {
        if(parseInt(p)==parseInt($scope.searchMap.pageNo)){
            return true;
        }else {
            return false;
        }
    }

    //设置排序规则
    $scope.sortSearch=function (sortField,sort) {
        $scope.searchMap.sortField = sortField;
        $scope.searchMap.sort = sort;
        $scope.search();
    }

    //判断关键字是不是品牌
    $scope.keywordsIsBrand=function () {
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            if($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0) {//如果包含
                return true;
            }
        }
        return false;
    }




})