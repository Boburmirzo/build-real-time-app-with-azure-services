const LOCAL_BASE_URL = 'http://127.0.0.1:7071';
const AZURE_BASE_URL = 'https://event-driven-java-stock-app.azurewebsites.net';

const getAPIBaseUrl = () => {
    const isLocal = /127.0.0.1/.test(window.location.href);
    //return isLocal ? LOCAL_BASE_URL : AZURE_BASE_URL;
    return AZURE_BASE_URL;
}

const app = new Vue({
    el: '#app',
    data() { 
        return {
            stocks: []
        }
    },
    methods: {
        async getStocks() {
            try {
                const apiUrl = `${getAPIBaseUrl()}/api/getStocks`;
                const response = await axios.get(apiUrl);
                console.log('Stocks fetched from ', apiUrl);
                app.stocks = response.data;
            } catch (ex) {
                console.error(ex);
            }
        }
    },
    created() {
        this.getStocks();
    }
});

const connect = () => {
    const connection = new signalR.HubConnectionBuilder().withUrl(`${getAPIBaseUrl()}/api`).build();

    connection.onclose(()  => {
        console.log('SignalR connection disconnected');
        setTimeout(() => connect(), 2000);
    });

    connection.on('updated', updatedStock => {
        const index = app.stocks.findIndex(s => s.id === updatedStock.id);
        app.stocks.splice(index, 1, updatedStock);
    });

    connection.start().then(() => {
        console.log("SignalR connection established");
    });
};

connect();
