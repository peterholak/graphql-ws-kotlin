const path = require('path')
const webpack = require('webpack')
const HtmlWebpackPlugin = require('html-webpack-plugin')
const WebpackCleanupPlugin = require('webpack-cleanup-plugin')
const CopyWebpackPlugin = require('copy-webpack-plugin')

const config = {
    entry: {
        'app': './src/index.tsx'
    },
    output: {
        path: path.resolve(__dirname, 'dist'),
        publicPath: '/',
        filename: 'assets/[name].js'
        // filename: 'assets/[name].[chunkhash].js'
    },
    resolve: {
        extensions: ['.ts', '.tsx', '.js']
    },
    module: {
        rules: [
            {test: /\.tsx?$/, use: 'awesome-typescript-loader', exclude: /node_modules/}
        ]
    },
    devtool: 'sourcemap',
    plugins: [
        new webpack.optimize.CommonsChunkPlugin({
            name: 'vendor',
            minChunks: function (module) {
                return module.context && module.context.indexOf('node_modules') !== -1;
            }
        }),
        new HtmlWebpackPlugin({ template: 'src/index.html' }),
        new CopyWebpackPlugin([{ from: 'node_modules/graphiql/graphiql.css', to: 'assets/css' }])
    ],

    devServer: {
        historyApiFallback: true
    }
}

module.exports = config