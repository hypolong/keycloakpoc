import React  from 'react';
import axios from 'axios';

class Axios extends React.Component {
    //构造函数
    constructor() {
        super();
        //react定义数据
        this.state = {
        returnData:'ボタンをクリックしてデータ獲得する',
        redirectURL:'ボタンをクリックしてURLを獲得する',
        token:'ボタンをクリックしてURLを獲得するJWTトークン発行されてリダレクト',
        userinfo:'獲得待っている......',
        redirectInfo:'ボタンをクリックしてURLを獲得するJWTトークン発行されてリダレクト',
        user_id:'hypolong',
        user_password:'123456789'
        };
    };
    //请求接口的方法
    getData=()=>{
        //var  api='https://www.apiopen.top/weatherApi?city=%E4%B8%8A%E6%B5%B7';
        var  api='http://localhost:8080/hello';
        axios.get(api)
        .then((response) =>{
            // handle success
            console.log(response.data);
            //用到this需要注意指向，箭头函数
            this.setState({
                returnData:response.data
            })
        })
        .catch(function (error) {
            // handle error
            console.log(error);
        });
    };

    //获取URL的接口方法
   geturlFuc=()=>{
        //var  api='https://www.apiopen.top/weatherApi?city=%E4%B8%8A%E6%B5%B7';
        var  api='http://localhost:8080/getURL';
        axios.post(api,{"url":"http://localhost:3001"},{
                headers: {
                    'Content-Type': 'application/json;charset=UTF-8'
                }
        })
        .then((response) =>{
            // handle success
            console.log(response.data);
            //用到this需要注意指向，箭头函数
            this.setState({
                redirectURL:response.data
            });
        })
        .catch(function (error) {
            // handle error
            console.log(error);
        });
    };

    //获取token的方法
    getToken=()=>{
        var  api='http://localhost:3010/api/token?name=' + this.state.user_id + '&pwd=' + this.state.user_password;
        
        axios.get(api)
        .then((response) =>{
            // handle success
            console.log(response.data);
            //用到this需要注意指向，箭头函数
            this.setState({
                token:response.data.uid
            });
            //window.open(response.data.url+ "?token=" +response.data.uid, '_blank');
        })
        .catch(function (error) {
            // handle error
            console.log(error);
        });
    };

    //请求获取用户信息的方法
    getUserInfo=()=>{
        var  api='http://localhost:3010/api/user-info?token=' + this.state.token;
        console.log(this.state.token);
        axios.get(api)
        .then((response) =>{
            // handle success
            console.log(response.data);
            //用到this需要注意指向，箭头函数
            this.setState({
                userinfo:response.data
            });
        })
        .catch(function (error) {
            // handle error
            console.log(error);
        });
    };

    setUid = (event) => {
        this.setState({
            user_id:event.target.value
        });
    };

    setPWD = (event) => {
        this.setState({
            user_password:event.target.value
        });
    };

    render() {
        return (
        <div > 
        <h2>Backendを叩く</h2>
        <button onClick={this.getData}>直接BackendのAPIを叩く</button>
        <p>{this.state.returnData}</p>
        <button onClick={this.geturlFuc}>URLを獲得</button>
        <p>{this.state.redirectURL}</p>

        <input type="text" value={this.state.user_id} onChange={this.setUid} />
        &nbsp;&nbsp;
        <input type="text" value={this.state.user_password} onChange={this.setPWD} />

        <button onClick={this.getToken}>Token獲得(By BFF)</button>
        <p>{this.state.token}</p>
        <button onClick={this.getUserInfo}>詳しい情報を取る(By BFF)</button>
        <p>{this.state.userinfo}</p>
        <button >会員代理ログイン</button>
        <p>{'開発中......'}</p>    
        
        </div>
        )
    }
}
export default Axios;