(function(){

	var cache = {};

	this.tmpl = function tmpl(str, data){

		// load template (if cached), or get template -- then cache
		var fn = !/\W/.test(str) ? cache[str] = cache[str] || tmpl(document.getElementById(str).innerHTML) :

			new Function("obj", // template generator (which will be cached)
				"var p=[],print=function(){p.push.apply(p,arguments);};" +

				"with(obj){p.push('" + // introduce data as local vars using with(){}

				str // convert template into pure JavaScript
					.replace(/[\r\t\n]/g, " ")
					.split("<%").join("\t")
					.replace(/((^|%>)[^\t]*)'/g, "$1\r")
					.replace(/\t=(.*?)%>/g, "',$1,'")
					.split("\t").join("');")
					.split("%>").join("p.push('")
					.split("\r").join("\\'")
				+ "');}return p.join('');");

		// provide basic currying to the user
		return data ? fn( data ) : fn;
	};
})();