 //控制层
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService) {

	 $controller('baseController', {$scope: $scope});//继承

	 //读取列表数据绑定到表单中
	 $scope.findAll = function () {
		 goodsService.findAll().success(
			 function (response) {
				 $scope.list = response;
			 }
		 );
	 }

	 //分页
	 $scope.findPage = function (page, rows) {
		 goodsService.findPage(page, rows).success(
			 function (response) {
				 $scope.list = response.rows;
				 $scope.paginationConf.totalItems = response.total;//更新总记录数
			 }
		 );
	 }

	 //查询实体
	 $scope.findOne = function (id) {
		 var id = $location.search()['id'];//获取请求参数值
		 if(id==null){
		 	return;
		 }

		 goodsService.findOne(id).success(
			 function (response) {
				 $scope.entity = response;
				//添加到富文本编辑器中，对应到多文本域中
				 editor.html($scope.entity.goodsDesc.introduction);
				//将图片的信息显示
				 $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);

				 //显示扩展属性
				 $scope.entity.goodsDesc.customAttributeItems =  JSON.parse($scope.entity.goodsDesc.customAttributeItems);

				 //还原规格
				 $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);

				 //还原sku
				 for( var i=0;i<$scope.entity.itemList.length;i++ ){
					 $scope.entity.itemList[i].spec = JSON.parse( $scope.entity.itemList[i].spec);
				 }

			 }
		 );
	 }

	 //保存
	 $scope.save = function () {

	 	//提取文本编辑器的值
		 $scope.entity.goodsDesc.introduction=editor.html();

		 var serviceObject;//服务层对象
		 if ($scope.entity.goods.id != null) {//如果有ID
			 serviceObject = goodsService.update($scope.entity); //修改
		 } else {
			 serviceObject = goodsService.add($scope.entity);//增加
		 }
		 serviceObject.success(
			 function (response) {
				 if (response.success) {
					 //重新查询
					 location.href="goods.html";//跳转到商品列表页
					 // $scope.reloadList();//重新加载
				 } else {
					 alert(response.message);
				 }
			 }
		 );
	 }


	 //批量删除
	 $scope.dele = function () {
		 //获取选中的复选框
		 goodsService.dele($scope.selectIds).success(
			 function (response) {
				 if (response.success) {
					 $scope.reloadList();//刷新列表
					 $scope.selectIds = [];
				 }
			 }
		 );
	 }

	 $scope.searchEntity = {};//定义搜索对象

	 //搜索
	 $scope.search = function (page, rows) {
		 goodsService.search(page, rows, $scope.searchEntity).success(
			 function (response) {
				 $scope.list = response.rows;
				 $scope.paginationConf.totalItems = response.total;//更新总记录数
			 }
		 );
	 }

	 //添加
	 $scope.add = function () {
		 //从富文本编辑器中取出数据传给数据库中的变量 editor富文本编辑器中的变量
		 $scope.entity.goodsDesc.introduction = editor.html();

		 goodsService.add($scope.entity).success(function (response) {
			 if (response.success) {
				 alert('保存成功');
				 $scope.entity={ goodsDesc:{itemImages:[],specificationItems:[]}  };
				 //清空富文本编辑器
				 editor.html("");
			 } else {
				 alert(response.message);
			 }
		 })
	 }


	 $scope.entity = {goods: {}, goodsDesc: {itemImages: []}};//定义页面实体结构
	 //多图片上传
	 $scope.add_image_entity = function () {
		 $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	 }

	 //列表中移除图片
	 $scope.remove_image_entity = function (index) {
		 $scope.entity.goodsDesc.itemImages.splice(index, 1);
	 }

	 //上传图片
	 $scope.uploadFile = function () {
		 uploadService.uploadFile().success(function (response) {
			 if (response.success) {//上传成功 取出url
				 $scope.image_entity.url = response.message;//设置文件地址
			 } else {
				 alert(response.message);
			 }
		 }).error(function () {
			 alert("上传失败");
		 })
	 }

	 //查询一级分类
	 $scope.selectItemCat1List = function () {
		 itemCatService.findByParentId(0).success(function (response) {

			 $scope.itemCat1List = response;
		 })
	 }

	 //根据一级分类查询二级分类列表选项
	 $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {
		 //判断一级分类有选择具体分类值，再去获取二级分类的值
		 if (newValue) {
			 //查询二级分类
			 itemCatService.findByParentId(newValue).success(function (response) {
				 $scope.itemCat2List = response;
			 })
		 }
	 })

	 //根据二级级分类查询三级分类列表选项
	 $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {
		 //判断二级分类有选择具体分类值，再去获取三级分类的值
		 if (newValue) {
			 //查询二级分类
			 itemCatService.findByParentId(newValue).success(function (response) {
				 $scope.itemCat3List = response;
			 })
		 }
	 })

	 //根据三级分类信息获取模板类型的ID
	 $scope.$watch("entity.goods.category3Id", function (newValue, oldValue) {
		 //判断三级分类有选择具体分类值，再去获取模板类型的id值
		 if (newValue) {
			 //获取模板类型
			 itemCatService.findOne(newValue).success(function (response) {
				 $scope.entity.goods.typeTemplateId = response.typeId;
			 })
		 }
	 })

	 //根据模板ID，获取品牌信息,
	 $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
		//判断模板ID是否有值,再去获取品牌信息
		 if(newValue){
		 	  //根据模板ID查询模板信息
			 typeTemplateService.findOne(newValue).success(function (response) {
			 	//接收响应模板
				$scope.typeTemplate = response;
				 //品牌列表
				$scope.typeTemplate.brandIds = JSON.parse( $scope.typeTemplate.brandIds);
				//如果页面没有传id就是添加，有id就是修改
				 if($location.search()['id']==null){
					 //由于模板中品牌的信息是json串存储，所以需要进行转换
					 $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
				 }
			 })
			 //根据模板ID查询规格列表
			 typeTemplateService.findSpecList(newValue).success(function (response) {
				 $scope.specList = response;
			 })
		 }
	 })

	 //初始化一个存储商品扩展属性对象
	 $scope.entity={goodsDesc: {itemImages: [],specificationItems:[]}};

	 $scope.updateSpecAttribute=function ($event,name,value) {
		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',name);
		if(object != null){
			if($event.target.checked){
				//选中
				object.attributeValue.push(value);
			}else{
				//取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);//移除选项
				//如果选项都取消了，将此条记录都移除
				if(object.attributeValue.length == 0){
					$scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object,1));
				}
			}
		}else{
			$scope.entity.goodsDesc.specificationItems.push({"attributeName":name,"attributeValue":[value]});
		}
	 }

	//创建SKU列表
	 $scope.createItemList=function () {
		 //初始一个 不带规格
    	 $scope.entity.itemList=[{spec:{},price:0,num:9999,status:'0',isDefault:'0'}];
		 //初始
		 var items = $scope.entity.goodsDesc.specificationItems;
		 for(var i = 0;i<items.length;i++){
		 	$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		 }
	 }

	 //添加列值
	 addColumn = function (list,columnName,conlumnValues) {
	 	var newList=[];//新的集合
		 for(var i=0;i<list.length;i++){
		 	var oldRow = list[i];
		 	for(var j=0;j<conlumnValues.length;j++){
		 		//深层克隆
				var newRow = JSON.parse(JSON.stringify(oldRow));
				newRow.spec[columnName]=conlumnValues[j];
				newList.push(newRow);
			}
		 }
		 return newList;
	 }

	//设定状态的数组
	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态

	$scope.itemCatList=[];//商品分类列表
	//设置获取所有的分类
	$scope.findItemCatList = function () {
		itemCatService.findAll().success(
			function (response) {//[{id:,parenId:,name:,typeId:,},{}]
			for(var i=0;i<response.length;i++){
				$scope.itemCatList[response[i].id] = response[i].name;//-->[{27:name}]
			}
		})
	}

	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var items= $scope.entity.goodsDesc.specificationItems;
		var object= $scope.searchObjectByKey(items,'attributeName',specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}




})