<template>
    <div>
        <!-- 单个商品组件 -->
        <el-card class="card" shadow="hover" :body-style="{ padding: '0px' }">
            <img :src="require('../assets/code_photo.jpg')" class="image" alt="购买" :disabled="payBtnDisabled" @click="buy(product.id)">
            <div style="padding: 14px;">
                <span>{{product.title}}</span>
                <div class="bottom clearfix">
                    <div class="show_info">
                        <img :src="require('../assets/money_logo.png')" alt="飘飘币" class="money_logo">
                        <span class="price">￥{{product.price / 100}}</span>
                        <!-- <el-button type="text" class="btn_buy" :disabled="addBtnDisabled" @click="add_cart(product.id)">添加</el-button> -->
                        <el-checkbox class="check_add_cart" v-model="checked" label="购物车" @change="handleCheckboxChange(product.id)"></el-checkbox>
                    </div>
                </div>
            </div>
        </el-card>

        <!-- 二维码弹窗 -->
        <el-dialog
            title="请用微信扫描二维码"
            :visible.sync="codeDialogVisible"
            @close="closeDialog"
            width="30%"
            center>
            <!-- 这里展示二维码 -->
            <qriously v-bind:value="codeUrl" :size="300" style="text-align:center"/>
        </el-dialog>
    </div>
</template>

<script>
    import axios from 'axios'

    export default {
        name: 'Product',
        props:["product",],
        data(){
            return{
                payBtnDisabled:false,    // 购买按钮是否禁用
                // addBtnDisabled:false,       // 添加到购物车按钮是否禁用
                checked:false,              // 勾选框初始状态
                codeDialogVisible:false,    //展示支付二维码
                payType: 'wxpay', //支付方式
                codeUrl:"",     // 二维码链接
                orderNo:"",     // 订单编号
                timer:null,     // 定时器

                // lockReconnect: false, //是否真正建立连接
				// timeout: 58 * 1000, //58秒一次心跳
				// timeoutObj: null, //心跳心跳倒计时
				// serverTimeoutObj: null, //心跳倒计时
				// timeoutnum: null, //断开 重连倒计时

            }
        },
		// created() {
		// 	this.initWebSocket(this.orderNo);
		// },
		// destroyed() {
		// 	this.websock.close(); //离开路由之后断开websocket连接
		// },

        methods:{
            handleCheckboxChange(productId) {
                // checkbox值变化
                this.$emit('checkbox-change',productId, this.checked);
            },
            // 发起支付请求
            buy: function(productId){
                // 支付按钮禁用防止重复支付
                this.payBtnDisabled = true
                // 根据选择的卡片，获取当前商品的id
                console.log("当前选中的productId:" + productId)
                // 支付请求
                axios.get("http://localhost:7290/api/wx-pay/nativePay/" + productId).then(
                    Response=>{
                        const resp = Response.data
                        this.codeUrl = resp.data.code_url    // 获取二维码链接
                        console.log("codeUrl=", this.codeUrl)
                        this.orderNo = resp.data.order_no   // 获取订单号
                        console.log("订单号", this.orderNo)

    				    // const wsuri = "ws://localhost:8086/socketServer/1";
                        // let wsClient = new WebSocket(wsuri)
                        // wsClient.onmessage =  function (e) {
                        //     console.log('收到服务器响应', e.data)
                        // };

                        //打开二维码弹窗
                        this.codeDialogVisible = true
                        // 启动定时器
                        this.timer = setInterval(()=>{
                            // 查询订单是否支付成功
                            this.queryOrderStatus()
                        },5000)

                    },
                    Error=>{
                        console.log("发起微信支付请求失败")
                    }
                )
            },
            // 查询订单是否支付成功
            queryOrderStatus() {
                axios.get('http://localhost:7290/api/order-info/order-status/' + this.orderNo).then(
                    Response=>{
                        console.log("支付状态码：",Response.data.code)
                        // 支付成功
                        if (Response.data.code === 2000) {
                            console.log(this.payType)
                            // 清除定时器
                            clearInterval(this.timer)
                            // 关闭二维码弹窗
                            this.payBtnDisabled = false
                            // 1秒后跳转到支付成功页面
                            setTimeout(()=>{
                                this.$router.push({path: '/home'})
                            }, 1000)
                        }

                    },
                    Error=>{}
                )
                console.log("订单状态:")
            },
            // 关闭微信支付二维码并让支付按钮重新可用
            closeDialog() {
                console.log('关闭二维码')
                this.payBtnDisabled = false

                // 直接下载资源啥的

            },

			// currentTime() {
			// 	setInterval(this.formatDate, 500);
			// },
			// initWebSocket(orderNo) {
			// 	//初始化weosocket

			// 	this.websock = new WebSocket(wsuri);
			// 	// 客户端接收服务端数据时触发
			// 	this.websock.onmessage = function (e) {
            //         console.log('收到服务器响应', e.data)
            //     };




				// 连接建立时触发
				// this.websock.onopen = this.websocketonopen;
				// // 通信发生错误时触发
				// this.websock.onerror = this.websocketonerror;
				// // 连接关闭时触发
				// this.websock.onclose = this.websocketclose;
			// },
			// // 连接建立时触发
			// websocketonopen() {
			// 	//开启心跳
			// 	this.start();
			// 	//连接建立之后执行send方法发送数据
			// 	// let actions = {"room":"007854ce7b93476487c7ca8826d17eba","info":"1121212"};
			// 	// this.websocketsend(JSON.stringify(actions));
			// },
			// // 通信发生错误时触发
			// websocketonerror() {
			// 	console.log("出现错误");
			// 	this.reconnect();
			// },
			// // 客户端接收服务端数据时触发
			// websocketonmessage(e) {
			// 	console.log(e.data);
			// 	//收到服务器信息，心跳重置

			// 	this.reset();
			// },
			// websocketsend(Data) {
			// 	//数据发送
			// 	this.websock.send(Data);
			// },
			// // 连接关闭时触发
			// websocketclose(e) {
			// 	//关闭
			// 	console.log("断开连接", e);
			// 	//重连
			// 	this.reconnect();
			// },
			// reconnect() {
			// 	//重新连接
			// 	var that = this;
			// 	if (that.lockReconnect) {
			// 		return;
			// 	}
			// 	that.lockReconnect = true;
			// 	//没连接上会一直重连，设置延迟避免请求过多
			// 	that.timeoutnum && clearTimeout(that.timeoutnum);
			// 	that.timeoutnum = setTimeout(function () {
			// 		//新连接
			// 		that.initWebSocket();
			// 		that.lockReconnect = false;
			// 	}, 5000);
			// },
			// reset() {
			// 	//重置心跳
			// 	var that = this;
			// 	//清除时间
			// 	clearTimeout(that.timeoutObj);
			// 	clearTimeout(that.serverTimeoutObj);
			// 	//重启心跳
			// 	that.start();
			// },
			// start() {
			// 	//开启心跳
			// 	console.log("开启心跳");
			// 	var self = this;
			// 	self.timeoutObj && clearTimeout(self.timeoutObj);
			// 	self.serverTimeoutObj && clearTimeout(self.serverTimeoutObj);
			// 	self.timeoutObj = setTimeout(function () {
			// 		//这里发送一个心跳，后端收到后，返回一个心跳消息，
			// 		if (self.ws.readyState == 1) {
			// 			//如果连接正常
			// 			// self.ws.send("heartCheck"); //这里可以自己跟后端约定
			// 		} else {
			// 			//否则重连
			// 			self.reconnect();
			// 		}
			// 		self.serverTimeoutObj = setTimeout(function () {
			// 			//超时关闭
			// 			self.ws.close();
			// 		}, self.timeout);
			// 	}, self.timeout);
			// },


        }

    }
</script>

<style lang="css" scoped>
    .card {
        width: 200px;
        margin-bottom: 5px;
    }

    .bottom {
        margin-top: 13px;
        line-height: 12px;
    }

    /* 商品图片 */
    .image {
        width: 200px;
        display: block;
    }
    /* 展示信息的区域 */
    .show_info{
        position: relative;
    }
    /* 飘飘币 */
    .money_logo, .price {
        width: 20px;
        height: 20px;
        vertical-align: middle;
    }


    /* 勾选框 */
    .check_add_cart {
        position:absolute;
        right: 40px;
        padding: 0;
        float: right;
        width: 20px;
        height: 20px;
        vertical-align: middle;
    }

    .clearfix:before,
    .clearfix:after {
        display: table;
        content: "";
    }

    .clearfix:after {
        clear: both
    }

    /* 二维码弹窗下的说明 */
    .dialog-footer{
        float: left;
        opacity: 0.8;
    }
</style>