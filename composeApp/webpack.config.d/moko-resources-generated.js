// noinspection JSUnnecessarySemicolon
;(function(config) {
    const path = require('path');
    const MiniCssExtractPlugin = require('mini-css-extract-plugin');

    config.module.rules.push(
        {
            test: /\.json$/,
            include: [
                path.resolve(__dirname, "kotlin/localization")
            ],
            type: 'asset/resource',
            generator: {
                filename: 'localization/[name][ext]'
            }
        }
    );

    // 针对图片等其他资源
    config.module.rules.push(
        {
             test: /\.(png|jpg|jpeg|gif|svg|txt)$/, 
             include: [
                path.resolve(__dirname, "kotlin/assets"),
                path.resolve(__dirname, "kotlin/files"),
                path.resolve(__dirname, "kotlin/images"),
             ],
             type: 'asset/resource'
        }
    );
    
    config.plugins.push(new MiniCssExtractPlugin())
    config.module.rules.push(
        {
            test: /\.css$/,
            resource: [
                path.resolve(__dirname, "kotlin/fonts"),
            ],
            use: ['style-loader', 'css-loader']
        }
    )

    config.module.rules.push(
        {
            test: /\.(otf|ttf)?$/,
            resource: [
                path.resolve(__dirname, "kotlin/fonts"),
            ],
            type: 'asset/resource',
            generator: {
                filename: 'fonts/[name][ext]'
            }
        }
    )
})(config);