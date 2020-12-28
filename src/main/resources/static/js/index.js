layui.use('form',function(){
    let form = layui.form;
});
layui.use('layer', function(){
    let layer = layui.layer;
});

function startSpider() {
    let keyword = document.getElementById('keyword').value;
    let threadNum = document.getElementById('threadNum').value;
    let xhr = new XMLHttpRequest();
    let url = "/spider?keyword=" + keyword + "&threadNum=" + threadNum;
    xhr.open('GET', url, true);
    xhr.onreadystatechange = function() {
        if (xhr.status == 200) {
            layer.msg('启动成功！');
        }
    }
    xhr.send(null);
}