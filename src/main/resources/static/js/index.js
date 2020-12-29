layui.use('form',function(){
    let form = layui.form;
});
layui.use('layer', function(){
    let layer = layui.layer;
});

function startSpider() {
    let keyword = document.getElementById('keyword').value;
    if (keyword === null || keyword === '') {
        layer.msg('请输入关键词！');
        return;
    }
    let threadNum = document.getElementById('threadNum').value;
    if (threadNum === null || threadNum === '') {
        layer.msg('请输入线程数！');
        return;
    }
    let startPage = document.getElementById('startPage').value;

    let xhr = new XMLHttpRequest();
    let url = "/spider?keyword=" + keyword + "&threadNum=" + threadNum;
    if (startPage !== null) {
        url += "&startPage=" + startPage;
    }
    xhr.open('GET', url, true);
    xhr.onreadystatechange = function() {
        if (xhr.status == 200) {
            layer.msg('启动成功！');
        }
    }
    xhr.send(null);
}