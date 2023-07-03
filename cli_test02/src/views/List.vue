<template>
    <div>
        <el-row :gutter="10">
            <el-col :span="4" v-for="product in products" :key="product.id">
                <Product :product="product" @checkbox-change="handleCheckboxChange"/>
            </el-col>
        </el-row>

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

        <el-button type="primary" @click="table = true" style="margin-left: 16px;">购物车</el-button>

        <el-drawer
        title="购物车详情"
        :visible.sync="table"
        size="50%">
        <span class="all_fee">合计：{{sumCartTotalFee}}</span>
        <el-button type="primary" class="btn_buy_all" @click="toPay" style="margin-left: 20px;">清空购物车</el-button>
        <p></p>
        <el-table :data="productInCart">
            <el-table-column property="id" label="课程ID" width="150"></el-table-column>
            <el-table-column property="title" label="课程名称" width="150"></el-table-column>
            <el-table-column property="price" label="价格/元" width="200"></el-table-column>
            <el-table-column property="num" label="个数"></el-table-column>
            </el-table>
        </el-drawer>

    </div>
</template>

<script>
    import Product from '../components/Product.vue'
    import axios from 'axios'


    export default {
        name:'List',
        components:{Product},
        data(){
            return{
                drawer: false,          // 是否弹出抽屉（购物车）
                table: false,

                products:[],
                productInCart:[],       // 加入购物车的productId
                totalFee:0,             // cart总计金额

                codeDialogVisible:false,    //展示支付二维码
                payType: 'wxpay', //支付方式
                codeUrl:"",     // 二维码链接
                orderNo:"",     // 订单编号
                timer:null,     // 定时器
            }
        },
        computed: {
            sumCartTotalFee() {
                let totalFeeInCart = 0;
                this.productInCart.forEach(item => {
                    totalFeeInCart += item.price * 100;
                });
                return totalFeeInCart / 100
            }

        },
        created(){
            // 向后台请求数据
            axios.get("http://localhost:7290/api/product/list").then(
                Response=>{
                    var resp = Response.data
                    console.log("获取商品列表",JSON.stringify(resp))
                    this.products = resp.data.productList
                    console.log("",this.products)
                },
                Error=>{
                    console.log("获取数据失败")
                }
            )
        },
        methods: {
            handleCheckboxChange(productId, isChecked) {
                // 处理子组件的checkbox值变化
                console.log('Checkbox value:',isChecked, productId);
                // console.log("productId",typeof productId);
                // console.log("isChecked",typeof isChecked);

                // const obj = this.products[parseInt(productId) - 1]           // 不对
                const obj = this.products.find(target => target.id === productId);
                console.log(obj)

                // 如果 isChecked 为 true
                if (isChecked) {
                    // 将选中的product放入productInCart
                    this.productInCart.unshift({"id":obj.id,"title":obj.title, "price":obj.price / 100, "num":1})
                } else {
                    // 将取消勾选的product从productInCart剔除
                        // this.todo_list = this.todo_list.filter((todo)=>{
                        // return todo.id !== id
                        // })
                    console.log("剔除")
                    this.productInCart = this.productInCart.filter((pro) => {
                            return pro.id != obj.id
                    })
                }
            },
            // 清空购物车
            toPay() {
                console.log("清空购物车")
                this.totalFee = this.sumCartTotalFee * 100
                console.log("@@@", this.totalFee)
                axios.get("http://localhost:7290/api/wx-pay/nativepay/cart/" + this.totalFee).then(
                    Response=>{
                        const resp = Response.data
                        this.codeUrl = resp.data.code_url    // 获取二维码链接
                        console.log("codeUrl=", this.codeUrl)
                        this.orderNo = resp.data.order_no   // 获取订单号
                        console.log("订单号", this.orderNo)

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
            // 关闭二维码弹窗
            closeDialog() {
                console.log('关闭二维码')
                // 直接下载资源啥的
            },
        }
    }
</script>

<style lang="css" scoped>
    .all_fee {
        margin-right: 400px;
        margin-left: 10px;
    }
</style>