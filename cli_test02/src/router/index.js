import VueRouter from "vue-router"
import Orders from "../views/Orders.vue"
import Home from "../views/Home.vue"
import List from "../views/List.vue"
import Mine from "../views/Mine.vue"


const router = new VueRouter({
    mode:"hash",
    routes:[{
        path:'/home',
        component:Home
    },{
        path:'/orders',
        component:Orders,
        meta:{isAuth:true},
        // children:[{

        // },{}]
    },{
        path:'/list',
        component:List   
    },{
        path:'/mine',
        component:Mine
    }]
})

// 路由守卫
// router.beforeEach((to,from,next)=>{
//     // 在进入除首页的其他页面需要检查是否登陆
//     next()
// })

// router.afterEach((to,from,next)=>{
//     //在切换路由之后吗，必要的操作
//     next()
// })


export default router